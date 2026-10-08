package com.victor.ecommerce.presentation.rest.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.victor.ecommerce.application.security.PasswordHasher;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@TestProfile(AuthenticationIntegrationTestProfile.class)
class AuthenticationIntegrationTest {

    private static final String TEST_PASSWORD = "integration-test-password";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @TestHTTPResource("/api/auth/login")
    URI loginUri;

    @Inject
    DataSource dataSource;

    @Inject
    PasswordHasher passwordHasher;

    private String userEmail;
    private String adminEmail;
    private String userWithoutPasswordEmail;
    private String userProductName;

    @BeforeEach
    void createIsolatedUsers() throws Exception {
        String suffix = UUID.randomUUID().toString();
        userEmail = "login-user-" + suffix + "@example.test";
        adminEmail = "login-admin-" + suffix + "@example.test";
        userWithoutPasswordEmail = "login-empty-" + suffix + "@example.test";
        userProductName = "Auth test product " + suffix;

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(
                     "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?)")) {
            insertUser(statement, userEmail, passwordHasher.hash(TEST_PASSWORD), "USER");
            insertUser(statement, adminEmail, passwordHasher.hash(TEST_PASSWORD), "ADMIN");
            insertUser(statement, userWithoutPasswordEmail, passwordHasher.hash("different-test-password"), "USER");
        }
    }

    @AfterAll
    static void removeTemporarySigningKeys() throws Exception {
        AuthenticationIntegrationTestProfile.deleteTemporaryKeys();
    }

    @AfterEach
    void removeIsolatedDatabaseRows() throws Exception {
        try (var connection = dataSource.getConnection()) {
            try (var deleteProducts = connection.prepareStatement("DELETE FROM products WHERE name = ?")) {
                deleteProducts.setString(1, userProductName);
                deleteProducts.executeUpdate();
            }
            try (var deleteUsers = connection.prepareStatement("DELETE FROM users WHERE email IN (?, ?, ?)")) {
                deleteUsers.setString(1, userEmail);
                deleteUsers.setString(2, adminEmail);
                deleteUsers.setString(3, userWithoutPasswordEmail);
                deleteUsers.executeUpdate();
            }
        }
    }

    @Test
    void validUserLoginMustWorkOnProtectedEndpoint() throws Exception {
        HttpResponse<String> loginResponse = login(userEmail, TEST_PASSWORD);

        assertEquals(200, loginResponse.statusCode());
        JsonNode body = OBJECT_MAPPER.readTree(loginResponse.body());
        assertNotNull(body.get("accessToken"));
        assertEquals("Bearer", body.get("tokenType").asText());
        assertEquals(900, body.get("expiresIn").asLong());

        HttpResponse<String> protectedResponse = getProtected(body.get("accessToken").asText());

        assertEquals(200, protectedResponse.statusCode());
        assertEquals("private", protectedResponse.body());
    }

    @Test
    void persistedRoleMustControlAdminEndpointAccess() throws Exception {
        String userToken = accessToken(login(userEmail, TEST_PASSWORD));
        String adminToken = accessToken(login(adminEmail, TEST_PASSWORD));

        HttpResponse<String> denied = createProduct(userToken);
        HttpResponse<String> allowed = createProduct(adminToken);

        assertEquals(403, denied.statusCode());
        assertEquals(201, allowed.statusCode());
    }

    @Test
    void invalidCredentialsAndMissingPasswordMustReturnSameFailure() throws Exception {
        HttpResponse<String> wrongPassword = login(userEmail, "wrong-password");
        HttpResponse<String> unknownEmail = login("unknown-" + UUID.randomUUID() + "@example.test", TEST_PASSWORD);
        HttpResponse<String> missingPassword = login(userWithoutPasswordEmail, TEST_PASSWORD);

        assertEquals(401, wrongPassword.statusCode());
        assertEquals(wrongPassword.body(), unknownEmail.body());
        assertEquals(wrongPassword.body(), missingPassword.body());
    }

    @Test
    void expiredOrUnknownSignatureTokensMustBeRejected() throws Exception {
        String userId = userId(userEmail);
        String expiredToken = Jwt.issuer("ecommerce")
                .subject(userId)
                .claim("role", "USER")
                .expiresAt(Instant.now().minusSeconds(60))
                .sign();

        var unrelatedKeyPairGenerator = KeyPairGenerator.getInstance("RSA");
        unrelatedKeyPairGenerator.initialize(2048);
        var unrelatedKey = unrelatedKeyPairGenerator.generateKeyPair().getPrivate();
        String tokenSignedByUnknownKey = Jwt.issuer("ecommerce")
                .subject(userId)
                .claim("role", "USER")
                .expiresAt(Instant.now().plusSeconds(300))
                .sign(unrelatedKey);

        assertEquals(401, getProtected(expiredToken).statusCode());
        assertEquals(401, getProtected(tokenSignedByUnknownKey).statusCode());
    }

    private HttpResponse<String> login(String email, String password) throws Exception {
        URI endpoint = loginUri.resolve("/api/auth/login");
        String requestBody = OBJECT_MAPPER.writeValueAsString(new LoginRequest(email, password));
        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> getProtected(String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(loginUri.resolve("/api/security/private"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> createProduct(String token) throws Exception {
        URI endpoint = loginUri.resolve("/api/products");
        String requestBody = OBJECT_MAPPER.writeValueAsString(
                new ProductRequest(userProductName, "Authentication integration test product", "12.50", 1));
        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String accessToken(HttpResponse<String> response) throws Exception {
        assertEquals(200, response.statusCode());
        return OBJECT_MAPPER.readTree(response.body()).get("accessToken").asText();
    }

    private String userId(String email) throws Exception {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement("SELECT id FROM users WHERE email = ?")) {
            statement.setString(1, email);
            try (var result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalStateException("Integration test user was not found");
                }
                return result.getString(1);
            }
        }
    }

    private void insertUser(java.sql.PreparedStatement statement, String email, String hash, String role)
            throws Exception {
        statement.setString(1, "Authentication Test");
        statement.setString(2, email);
        statement.setString(3, hash);
        statement.setString(4, role);
        statement.executeUpdate();
    }

    private record ProductRequest(String name, String description, String price, int stock) {
    }
}
