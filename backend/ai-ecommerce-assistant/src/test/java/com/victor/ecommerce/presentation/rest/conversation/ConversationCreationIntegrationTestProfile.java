package com.victor.ecommerce.presentation.rest.conversation;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Map;

public class ConversationCreationIntegrationTestProfile implements QuarkusTestProfile {

    private static final Path KEY_DIRECTORY;
    private static final Path PRIVATE_KEY_PATH;
    private static final Path PUBLIC_KEY_PATH;

    static {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            KEY_DIRECTORY = Files.createTempDirectory("conversation-integration-test-");
            PRIVATE_KEY_PATH = KEY_DIRECTORY.resolve("private-key.pem");
            PUBLIC_KEY_PATH = KEY_DIRECTORY.resolve("public-key.pem");
            writePem(PRIVATE_KEY_PATH, "PRIVATE KEY", keyPair.getPrivate().getEncoded());
            writePem(PUBLIC_KEY_PATH, "PUBLIC KEY", keyPair.getPublic().getEncoded());
        } catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "mp.jwt.verify.publickey.location", PUBLIC_KEY_PATH.toString(),
                "smallrye.jwt.sign.key.location", PRIVATE_KEY_PATH.toString(),
                "smallrye.jwt.path.groups", "role",
                "quarkus.langchain4j.openai.api-key", "integration-test-placeholder"
        );
    }

    static void deleteTemporaryKeys() throws IOException {
        Files.deleteIfExists(PRIVATE_KEY_PATH);
        Files.deleteIfExists(PUBLIC_KEY_PATH);
        Files.deleteIfExists(KEY_DIRECTORY);
    }

    private static void writePem(Path path, String type, byte[] encodedKey) throws IOException {
        String content = "-----BEGIN " + type + "-----\n"
                + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(encodedKey)
                + "\n-----END " + type + "-----\n";
        Files.writeString(path, content);
    }
}
