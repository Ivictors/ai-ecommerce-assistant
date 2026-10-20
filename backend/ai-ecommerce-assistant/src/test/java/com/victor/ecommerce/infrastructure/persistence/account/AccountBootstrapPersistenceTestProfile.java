package com.victor.ecommerce.infrastructure.persistence.account;

import io.quarkus.test.junit.QuarkusTestProfile;
import org.eclipse.microprofile.config.ConfigProvider;
import org.postgresql.ds.PGSimpleDataSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

public class AccountBootstrapPersistenceTestProfile implements QuarkusTestProfile {

    private static final String SCHEMA = "account_bootstrap_test_" + UUID.randomUUID().toString().replace("-", "");
    private static final Path KEY_DIRECTORY;
    private static final Path PRIVATE_KEY;
    private static final Path PUBLIC_KEY;

    static {
        try {
            var config = ConfigProvider.getConfig();
            var dataSource = new PGSimpleDataSource();
            dataSource.setURL(config.getOptionalValue("quarkus.datasource.jdbc.url", String.class)
                    .orElse("jdbc:postgresql://localhost:5432/ecommerce"));
            dataSource.setUser(config.getOptionalValue("quarkus.datasource.username", String.class).orElse("ecommerce"));
            dataSource.setPassword(config.getOptionalValue("quarkus.datasource.password", String.class).orElse("ecommerce"));
            try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
                statement.execute("CREATE EXTENSION IF NOT EXISTS vector SCHEMA public");
                statement.execute("CREATE SCHEMA " + SCHEMA);
            }

            var generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            var pair = generator.generateKeyPair();
            KEY_DIRECTORY = Files.createTempDirectory("account-bootstrap-test-");
            PRIVATE_KEY = KEY_DIRECTORY.resolve("private-key.pem");
            PUBLIC_KEY = KEY_DIRECTORY.resolve("public-key.pem");
            writePem(PRIVATE_KEY, "PRIVATE KEY", pair.getPrivate().getEncoded());
            writePem(PUBLIC_KEY, "PUBLIC KEY", pair.getPublic().getEncoded());
        } catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    @Override
    public Map<String, String> getConfigOverrides() {
        String url = ConfigProvider.getConfig().getOptionalValue("quarkus.datasource.jdbc.url", String.class)
                .orElse("jdbc:postgresql://localhost:5432/ecommerce");
        String schemaUrl = url + (url.contains("?") ? "&" : "?") + "currentSchema=" + SCHEMA + ",public";
        return Map.of(
                "quarkus.datasource.jdbc.url", schemaUrl,
                "quarkus.flyway.schemas", SCHEMA,
                "quarkus.flyway.default-schema", SCHEMA,
                "quarkus.hibernate-orm.database.default-schema", SCHEMA,
                "mp.jwt.verify.publickey.location", PUBLIC_KEY.toString(),
                "smallrye.jwt.sign.key.location", PRIVATE_KEY.toString(),
                "smallrye.jwt.path.groups", "role",
                "quarkus.langchain4j.openai.api-key", "integration-test-placeholder"
        );
    }

    static String schemaName() {
        return SCHEMA;
    }

    static void dropIsolatedSchema() throws Exception {
        if (!SCHEMA.matches("account_bootstrap_test_[a-f0-9]{32}")) {
            throw new IllegalStateException("Refusing to drop an unrecognized test schema");
        }
        var config = ConfigProvider.getConfig();
        var dataSource = new PGSimpleDataSource();
        dataSource.setURL(config.getOptionalValue("quarkus.datasource.jdbc.url", String.class)
                .orElse("jdbc:postgresql://localhost:5432/ecommerce"));
        dataSource.setUser(config.getOptionalValue("quarkus.datasource.username", String.class).orElse("ecommerce"));
        dataSource.setPassword(config.getOptionalValue("quarkus.datasource.password", String.class).orElse("ecommerce"));
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.execute("DROP SCHEMA IF EXISTS " + SCHEMA + " CASCADE");
        }
        Files.deleteIfExists(PRIVATE_KEY);
        Files.deleteIfExists(PUBLIC_KEY);
        Files.deleteIfExists(KEY_DIRECTORY);
    }

    private static void writePem(Path path, String type, byte[] encoded) throws Exception {
        String pem = "-----BEGIN " + type + "-----\n"
                + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(encoded)
                + "\n-----END " + type + "-----\n";
        Files.writeString(path, pem);
    }
}
