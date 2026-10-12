package com.victor.ecommerce.presentation.rest.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.victor.ecommerce.application.account.AccountBootstrapService;
import com.victor.ecommerce.presentation.cli.AccountBootstrapCommand;
import com.victor.ecommerce.presentation.cli.ConsolePasswordPrompt;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestProfile(AuthenticationIntegrationTestProfile.class)
class AccountBootstrapIntegrationTest {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String ADMIN_PASSWORD = "bootstrap-admin-secret";
    private static final String USER_PASSWORD = "bootstrap-user-secret";

    @TestHTTPResource("/api/auth/login")
    URI loginUri;

    @Inject
    AccountBootstrapService bootstrapService;

    @Inject
    DataSource dataSource;

    private String adminEmail;
    private String userEmail;

    @BeforeEach
    void defineAccounts() {
        String suffix = UUID.randomUUID().toString();
        adminEmail = "bootstrap-admin-" + suffix + "@example.test";
        userEmail = "bootstrap-user-" + suffix + "@example.test";
    }

    @AfterEach
    void removeAccounts() throws Exception {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement("DELETE FROM users WHERE email IN (?, ?)")) {
            statement.setString(1, adminEmail);
            statement.setString(2, userEmail);
            statement.executeUpdate();
        }
    }

    @AfterAll
    static void removeTemporarySigningKeys() throws Exception {
        AuthenticationIntegrationTestProfile.deleteTemporaryKeys();
    }

    @Test
    void test_AC001_AC002_AC006_AC009_T005T1_T005T2_T005T3_bootstrapAccountsAuthenticateWithPersistedRoles()
            throws Exception {
        ConsolePasswordPrompt prompt = mock(ConsolePasswordPrompt.class);
        when(prompt.isAvailable()).thenReturn(true);
        when(prompt.readLine("administrator name: ")).thenReturn("Bootstrap Admin");
        when(prompt.readLine("administrator email: ")).thenReturn(adminEmail);
        when(prompt.readConfirmedPassword("administrator password: ")).thenReturn(ADMIN_PASSWORD.toCharArray());
        when(prompt.readLine("test user name: ")).thenReturn("Bootstrap User");
        when(prompt.readLine("test user email: ")).thenReturn(userEmail);
        when(prompt.readConfirmedPassword("test user password: ")).thenReturn(USER_PASSWORD.toCharArray());
        var output = new ByteArrayOutputStream();

        int exitCode = new AccountBootstrapCommand(
                bootstrapService, prompt, new PrintStream(output, true, StandardCharsets.UTF_8))
                .run(new String[]{"--test-user"});

        assertEquals(AccountBootstrapCommand.SUCCESS, exitCode);
        assertEquals(1, countUsers(adminEmail, "ADMIN"));
        assertEquals(1, countUsers(userEmail, "USER"));
        assertFalse(output.toString(StandardCharsets.UTF_8).contains(ADMIN_PASSWORD));
        assertFalse(output.toString(StandardCharsets.UTF_8).contains(USER_PASSWORD));
        assertFalse(output.toString(StandardCharsets.UTF_8).contains("password_hash"));

        JsonNode adminLogin = OBJECT_MAPPER.readTree(login(adminEmail, ADMIN_PASSWORD).body());
        JsonNode userLogin = OBJECT_MAPPER.readTree(login(userEmail, USER_PASSWORD).body());
        assertNotNull(adminLogin.get("accessToken"));
        assertNotNull(userLogin.get("accessToken"));
        assertEquals("ADMIN", tokenPayload(adminLogin.get("accessToken").asText()).get("role").asText());
        assertEquals("USER", tokenPayload(userLogin.get("accessToken").asText()).get("role").asText());
    }

    @Test
    void test_AC005_AC010_T005T4_bootstrapRejectsSecondAttemptWithoutAddingAccounts() throws Exception {
        insertUser(adminEmail, "ADMIN");
        int before = countAllUsers();

        var request = new AccountBootstrapService.BootstrapRequest(
                new AccountBootstrapService.AccountDetails("Second Admin", adminEmail, ADMIN_PASSWORD),
                java.util.Optional.empty());

        RuntimeException failure = assertThrows(RuntimeException.class, () -> bootstrapService.bootstrap(request));
        assertTrue(failure.toString().contains("ADMIN_ALREADY_EXISTS"));

        assertEquals(before, countAllUsers());
    }

    private HttpResponse<String> login(String email, String password) throws Exception {
        String body = OBJECT_MAPPER.writeValueAsString(new LoginRequest(email, password));
        HttpRequest request = HttpRequest.newBuilder(loginUri.resolve("/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        return response;
    }

    private JsonNode tokenPayload(String token) throws Exception {
        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
        return OBJECT_MAPPER.readTree(payload);
    }

    private int countUsers(String email, String role) throws Exception {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE email = ? AND role = ?")) {
            statement.setString(1, email);
            statement.setString(2, role);
            try (var result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    private int countAllUsers() throws Exception {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT COUNT(*) FROM users")) {
            result.next();
            return result.getInt(1);
        }
    }

    private void insertUser(String email, String role) throws Exception {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(
                     "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, "Existing Admin");
            statement.setString(2, email);
            statement.setString(3, "existing-hash");
            statement.setString(4, role);
            statement.executeUpdate();
        }
    }

    private record LoginRequest(String email, String password) {
    }
}
