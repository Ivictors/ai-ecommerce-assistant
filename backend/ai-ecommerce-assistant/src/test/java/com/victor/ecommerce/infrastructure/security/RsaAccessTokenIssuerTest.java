package com.victor.ecommerce.infrastructure.security;

import com.victor.ecommerce.application.security.AccessTokenIssuer;
import com.victor.ecommerce.domain.user.UserRole;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPairGenerator;
import java.time.Duration;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RsaAccessTokenIssuerTest {

    @Test
    void tokenMustContainNumericSubjectRoleAndIssuer() throws Exception {
        Path keyFile = Files.createTempFile("jwt-test-", ".pem");
        try {
            var generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            byte[] encodedKey = generator.generateKeyPair().getPrivate().getEncoded();
            String pem = "-----BEGIN PRIVATE KEY-----\n"
                    + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(encodedKey)
                    + "\n-----END PRIVATE KEY-----\n";
            Files.writeString(keyFile, pem, StandardCharsets.US_ASCII);
            System.setProperty("smallrye.jwt.sign.key.location", keyFile.toString());

            AccessTokenIssuer.IssuedAccessToken issued =
                    new RsaAccessTokenIssuer("ecommerce", Duration.ofMinutes(15))
                            .issue(42L, UserRole.ADMIN);
            String payload = new String(
                    Base64.getUrlDecoder().decode(issued.value().split("\\.")[1]),
                    StandardCharsets.UTF_8);

            assertTrue(payload.contains("\"sub\":\"42\""));
            assertTrue(payload.contains("\"role\":\"ADMIN\""));
            assertTrue(payload.contains("\"iss\":\"ecommerce\""));
            assertTrue(issued.expiresInSeconds() == 900);
        } finally {
            System.clearProperty("smallrye.jwt.sign.key.location");
            Files.deleteIfExists(keyFile);
        }
    }
}
