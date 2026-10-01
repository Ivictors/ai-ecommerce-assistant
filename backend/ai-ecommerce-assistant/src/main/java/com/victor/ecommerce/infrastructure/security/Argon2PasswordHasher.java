package com.victor.ecommerce.infrastructure.security;

import com.kosprov.jargon2.api.Jargon2;
import com.kosprov.jargon2.api.Jargon2.Hasher;
import com.victor.ecommerce.application.security.PasswordHasher;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.charset.StandardCharsets;

import static com.kosprov.jargon2.api.Jargon2.jargon2Hasher;
import static com.kosprov.jargon2.api.Jargon2.jargon2Verifier;

@ApplicationScoped
public class Argon2PasswordHasher implements PasswordHasher {

    private final int memoryInKiB;
    private final int iterations;
    private final int parallelism;
    private final int hashLength;

    public Argon2PasswordHasher(
            @ConfigProperty(name = "security.password.argon2.memory-kib", defaultValue = "65536") int memoryInKiB,
            @ConfigProperty(name = "security.password.argon2.iterations", defaultValue = "3") int iterations,
            @ConfigProperty(name = "security.password.argon2.parallelism", defaultValue = "1") int parallelism,
            @ConfigProperty(name = "security.password.argon2.hash-length", defaultValue = "32") int hashLength) {
        this.memoryInKiB = memoryInKiB;
        this.iterations = iterations;
        this.parallelism = parallelism;
        this.hashLength = hashLength;
    }

    @Override
    public String hash(String rawPassword) {
        return hasher()
                .password(rawPassword.getBytes(StandardCharsets.UTF_8))
                .encodedHash();
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null || encodedPassword.isBlank()) {
            return false;
        }
        return jargon2Verifier()
                .hash(encodedPassword)
                .password(rawPassword.getBytes(StandardCharsets.UTF_8))
                .verifyEncoded();
    }

    private Hasher hasher() {
        return jargon2Hasher()
                .type(Jargon2.Type.ARGON2id)
                .memoryCost(memoryInKiB)
                .timeCost(iterations)
                .parallelism(parallelism)
                .hashLength(hashLength)
                .saltLength(16);
    }
}
