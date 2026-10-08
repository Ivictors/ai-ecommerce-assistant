package com.victor.ecommerce.infrastructure.persistence.account;

import com.victor.ecommerce.presentation.rest.conversation.ConversationCreationIntegrationTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.eclipse.microprofile.config.ConfigProvider;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestProfile(ConversationCreationIntegrationTestProfile.class)
class AccountProvisioningMigrationIntegrationTest {

    private static final String[] SEEDED_EMAILS = {
            "victor@example.com",
            "ana@example.com",
            "carlos@example.com"
    };

    private DataSource migrationDataSource;
    private String schemaName;

    @BeforeEach
    void createSchemaAtVersionSix() throws Exception {
        var config = ConfigProvider.getConfig();
        var postgresDataSource = new PGSimpleDataSource();
        postgresDataSource.setURL(config.getValue("quarkus.datasource.jdbc.url", String.class));
        postgresDataSource.setUser(config.getValue("quarkus.datasource.username", String.class));
        postgresDataSource.setPassword(config.getValue("quarkus.datasource.password", String.class));
        migrationDataSource = postgresDataSource;

        schemaName = "account_provisioning_test_" + UUID.randomUUID().toString().replace("-", "");
        try (var connection = migrationDataSource.getConnection(); var statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA " + schemaName);
        }
        flyway(MigrationVersion.fromVersion("6")).migrate();
    }

    @AfterEach
    void dropOnlyThisTestSchema() throws Exception {
        if (schemaName == null || !schemaName.matches("account_provisioning_test_[a-f0-9]{32}")) {
            return;
        }
        try (var connection = migrationDataSource.getConnection(); var statement = connection.createStatement()) {
            statement.execute("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE");
        }
    }

    @Test
    void test_AC007_T001T1_cleanMigrationRemovesOnlySeedAccountsAndCreatesAuditSchema() throws Exception {
        flyway(null).migrate();

        assertEquals(0, countSeededUsers());
        assertEquals(0, count("orders"));
        assertEquals(0, count("order_items"));
        assertEquals(0, count("conversations"));
        assertEquals(7, count("products"));
        assertEquals("NO", passwordHashIsNullable());
        assertTrue(tableExists("account_provisioning_audit"));
        assertEquals("9", flyway(null).info().current().getVersion().getVersion());
    }

    @Test
    void test_AC007_T001T2_migrationPreservesUnrelatedUserAndDependentData() throws Exception {
        insertUnrelatedAccountAndData();
        flyway(null).migrate();

        assertEquals(0, countSeededUsers());
        assertEquals(1, countUser("unrelated-user@example.test"));
        assertEquals(1, countRelated("orders", "unrelated-user@example.test"));
        assertEquals(1, countRelated("conversations", "unrelated-user@example.test"));
        assertEquals(1, countOrderItemsFor("unrelated-user@example.test"));
        assertEquals(7, count("products"));
    }

    @Test
    void test_AC007_T001T3_databaseRejectsNullPasswordHashAfterMigration() throws Exception {
        flyway(null).migrate();

        assertThrows(SQLException.class, this::insertUserWithNullPasswordHash);
        assertEquals(0, countUser("null-hash@example.test"));
    }

    private Flyway flyway(MigrationVersion target) {
        var configuration = Flyway.configure()
                .dataSource(migrationDataSource)
                .schemas(schemaName)
                .defaultSchema(schemaName)
                .locations("classpath:db/migration");
        if (target != null) {
            configuration.target(target);
        }
        return configuration.load();
    }

    private void insertUnrelatedAccountAndData() throws Exception {
        try (var connection = migrationDataSource.getConnection()) {
            connection.setSchema(schemaName);
            try (var user = connection.prepareStatement(
                    "INSERT INTO users (name, email, password_hash) VALUES (?, ?, ?)")) {
                user.setString(1, "Unrelated Account");
                user.setString(2, "unrelated-user@example.test");
                user.setString(3, "test-only-non-null-hash");
                user.executeUpdate();
            }
            try (var order = connection.prepareStatement(
                    "INSERT INTO orders (user_id, status, total) "
                            + "SELECT id, 'PENDING', 10 FROM users WHERE email = ?")) {
                order.setString(1, "unrelated-user@example.test");
                order.executeUpdate();
            }
            try (var item = connection.prepareStatement(
                    "INSERT INTO order_items (order_id, product_id, quantity, unit_price) "
                            + "SELECT o.id, p.id, 1, 10 FROM orders o "
                            + "JOIN users u ON u.id = o.user_id "
                            + "CROSS JOIN (SELECT id FROM products ORDER BY id LIMIT 1) p "
                            + "WHERE u.email = ?")) {
                item.setString(1, "unrelated-user@example.test");
                item.executeUpdate();
            }
            try (var conversation = connection.prepareStatement(
                    "INSERT INTO conversations (user_id) SELECT id FROM users WHERE email = ?")) {
                conversation.setString(1, "unrelated-user@example.test");
                conversation.executeUpdate();
            }
        }
    }

    private void insertUserWithNullPasswordHash() throws Exception {
        try (var connection = migrationDataSource.getConnection();
             var statement = connection.prepareStatement(
                     "INSERT INTO " + schemaName + ".users (name, email, password_hash) VALUES (?, ?, NULL)")) {
            statement.setString(1, "Invalid Account");
            statement.setString(2, "null-hash@example.test");
            statement.executeUpdate();
        }
    }

    private int countSeededUsers() throws Exception {
        String sql = "SELECT COUNT(*) FROM " + schemaName + ".users WHERE email IN (?, ?, ?)";
        try (var connection = migrationDataSource.getConnection(); var statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < SEEDED_EMAILS.length; index++) {
                statement.setString(index + 1, SEEDED_EMAILS[index]);
            }
            try (var result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    private int count(String tableName) throws Exception {
        try (var connection = migrationDataSource.getConnection();
             var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT COUNT(*) FROM " + schemaName + "." + tableName)) {
            result.next();
            return result.getInt(1);
        }
    }

    private int countUser(String email) throws Exception {
        return countForEmail("SELECT COUNT(*) FROM " + schemaName + ".users WHERE email = ?", email);
    }

    private int countRelated(String tableName, String email) throws Exception {
        String sql = "SELECT COUNT(*) FROM " + schemaName + "." + tableName + " related "
                + "JOIN " + schemaName + ".users u ON u.id = related.user_id WHERE u.email = ?";
        return countForEmail(sql, email);
    }

    private int countOrderItemsFor(String email) throws Exception {
        String sql = "SELECT COUNT(*) FROM " + schemaName + ".order_items oi "
                + "JOIN " + schemaName + ".orders o ON o.id = oi.order_id "
                + "JOIN " + schemaName + ".users u ON u.id = o.user_id WHERE u.email = ?";
        return countForEmail(sql, email);
    }

    private int countForEmail(String sql, String email) throws Exception {
        try (var connection = migrationDataSource.getConnection(); var statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (var result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    private String passwordHashIsNullable() throws Exception {
        String sql = "SELECT is_nullable FROM information_schema.columns "
                + "WHERE table_schema = ? AND table_name = 'users' AND column_name = 'password_hash'";
        try (var connection = migrationDataSource.getConnection(); var statement = connection.prepareStatement(sql)) {
            statement.setString(1, schemaName);
            try (var result = statement.executeQuery()) {
                assertTrue(result.next());
                return result.getString(1);
            }
        }
    }

    private boolean tableExists(String tableName) throws Exception {
        String sql = "SELECT EXISTS (SELECT 1 FROM information_schema.tables "
                + "WHERE table_schema = ? AND table_name = ?)";
        try (var connection = migrationDataSource.getConnection(); var statement = connection.prepareStatement(sql)) {
            statement.setString(1, schemaName);
            statement.setString(2, tableName);
            try (var result = statement.executeQuery()) {
                result.next();
                return result.getBoolean(1);
            }
        }
    }
}
