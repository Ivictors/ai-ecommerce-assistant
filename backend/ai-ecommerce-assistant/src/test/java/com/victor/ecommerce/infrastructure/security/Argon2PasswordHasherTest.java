package com.victor.ecommerce.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Argon2PasswordHasherTest {

    private final Argon2PasswordHasher hasher = new Argon2PasswordHasher(16, 1, 1, 32);

    @Test
    void hashMustNotEqualRawPassword() {
        String rawPassword = "correct-password";

        assertNotEquals(rawPassword, hasher.hash(rawPassword));
    }

    @Test
    void matchingPasswordMustBeAccepted() {
        String hash = hasher.hash("correct-password");

        assertTrue(hasher.matches("correct-password", hash));
    }

    @Test
    void differentPasswordMustBeRejected() {
        String hash = hasher.hash("correct-password");

        assertFalse(hasher.matches("wrong-password", hash));
    }
}
