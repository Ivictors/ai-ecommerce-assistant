package com.victor.ecommerce.presentation.rest.conversation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestProfile(ConversationCreationIntegrationTestProfile.class)
class ConversationCreationIntegrationTest {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @TestHTTPResource("/api/conversations")
    URI conversationsUri;

    @Inject
    DataSource dataSource;

    private String userEmail;
    private String otherUserEmail;
    private String adminEmail;

    @BeforeEach
    void createIsolatedUsers() throws Exception {
        String suffix = UUID.randomUUID().toString();
        userEmail = "conversation-user-" + suffix + "@example.test";
        otherUserEmail = "conversation-other-" + suffix + "@example.test";
        adminEmail = "conversation-admin-" + suffix + "@example.test";
        insertUser(userEmail, "USER");
        insertUser(otherUserEmail, "USER");
        insertUser(adminEmail, "ADMIN");
    }

    @AfterEach
    void removeIsolatedRows() throws Exception {
        try (var connection = dataSource.getConnection()) {
            try (var deleteConversations = connection.prepareStatement(
                    "DELETE FROM conversations WHERE user_id IN "
                            + "(SELECT id FROM users WHERE email IN (?, ?, ?))")) {
                deleteConversations.setString(1, userEmail);
                deleteConversations.setString(2, otherUserEmail);
                deleteConversations.setString(3, adminEmail);
                deleteConversations.executeUpdate();
            }
            try (var deleteUsers = connection.prepareStatement(
                    "DELETE FROM users WHERE email IN (?, ?, ?)")) {
                deleteUsers.setString(1, userEmail);
                deleteUsers.setString(2, otherUserEmail);
                deleteUsers.setString(3, adminEmail);
                deleteUsers.executeUpdate();
            }
        }
    }

    @AfterAll
    static void removeTemporarySigningKeys() throws Exception {
        ConversationCreationIntegrationTestProfile.deleteTemporaryKeys();
    }

    @Test
    void test_AC001_T001T2_authenticatedUserCreatesOwnedConversation() throws Exception {
        String token = tokenFor(userEmail, "USER");

        HttpResponse<String> response = createConversation(token, "{}");

        assertEquals(201, response.statusCode());
        JsonNode created = OBJECT_MAPPER.readTree(response.body());
        assertNotNull(created.get("id"));
        assertEquals(userId(userEmail), created.get("userId").asText());
        assertNotNull(created.get("createdAt"));
    }

    @Test
    void test_AC002_T001T6_createdConversationAppearsInCreatorList() throws Exception {
        String token = tokenFor(userEmail, "USER");
        HttpResponse<String> createdResponse = createConversation(token, "{}");
        JsonNode created = OBJECT_MAPPER.readTree(createdResponse.body());

        HttpResponse<String> listResponse = getConversations(token);

        assertEquals(200, listResponse.statusCode());
        JsonNode conversations = OBJECT_MAPPER.readTree(listResponse.body());
        boolean found = false;
        for (JsonNode conversation : conversations) {
            found |= created.get("id").asLong() == conversation.get("id").asLong();
        }
        assertTrue(found);
    }

    @Test
    void test_AC003_T001T3_anonymousCallerCannotCreateConversation() throws Exception {
        int initialCount = conversationCount();

        HttpResponse<String> response = createConversation(null, "{}");

        assertEquals(401, response.statusCode());
        assertEquals(initialCount, conversationCount());
    }

    @Test
    void test_AC003_T001T4_nonUserRoleCannotCreateConversation() throws Exception {
        int initialCount = conversationCount();

        HttpResponse<String> response = createConversation(tokenFor(adminEmail, "ADMIN"), "{}");

        assertEquals(403, response.statusCode());
        assertEquals(initialCount, conversationCount());
    }

    @Test
    void test_AC003_T001T9_tokenWithoutPersistedUserCannotCreateConversation() throws Exception {
        int initialCount = conversationCount();

        HttpResponse<String> response = createConversation(tokenForSubject(Long.MAX_VALUE, "USER"), "{}");

        assertEquals(401, response.statusCode());
        assertEquals(initialCount, conversationCount());
    }

    @Test
    void test_AC004_T001T5_clientCannotChooseConversationOwner() throws Exception {
        HttpResponse<String> response = createConversation(
                tokenFor(userEmail, "USER"),
                "{\"userId\":" + userId(otherUserEmail) + "}"
        );

        assertEquals(201, response.statusCode());
        JsonNode created = OBJECT_MAPPER.readTree(response.body());
        assertEquals(userId(userEmail), created.get("userId").asText());
    }

    @Test
    void test_AC005_T001T7_otherUserCannotUseConversationInChat() throws Exception {
        HttpResponse<String> createdResponse = createConversation(tokenFor(userEmail, "USER"), "{}");
        assertEquals(201, createdResponse.statusCode());
        JsonNode created = OBJECT_MAPPER.readTree(createdResponse.body());

        HttpRequest request = HttpRequest.newBuilder(conversationsUri.resolve(
                        "/api/chat?conversationId=" + created.get("id").asLong()))
                .header("Authorization", "Bearer " + tokenFor(otherUserEmail, "USER"))
                .header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.ofString("Hello"))
                .build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    private HttpResponse<String> createConversation(String token, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(conversationsUri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return HTTP_CLIENT.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> getConversations(String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(conversationsUri)
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String tokenFor(String email, String role) throws Exception {
        return tokenForSubject(Long.parseLong(userId(email)), role);
    }

    private String tokenForSubject(long userId, String role) {
        return Jwt.issuer("ecommerce")
                .subject(Long.toString(userId))
                .claim("role", role)
                .expiresIn(300)
                .sign();
    }

    private void insertUser(String email, String role) throws Exception {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(
                     "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, "Conversation Integration Test");
            statement.setString(2, email);
            statement.setString(3, "test-only-password-hash");
            statement.setString(4, role);
            statement.executeUpdate();
        }
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

    private int conversationCount() throws Exception {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT COUNT(*) FROM conversations")) {
            result.next();
            return result.getInt(1);
        }
    }
}
