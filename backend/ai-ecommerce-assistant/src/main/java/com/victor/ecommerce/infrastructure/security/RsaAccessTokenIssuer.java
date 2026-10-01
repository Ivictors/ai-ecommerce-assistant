package com.victor.ecommerce.infrastructure.security;

import com.victor.ecommerce.application.security.AccessTokenIssuer;
import com.victor.ecommerce.domain.user.UserRole;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;

@ApplicationScoped
public class RsaAccessTokenIssuer implements AccessTokenIssuer {

    private final String issuer;
    private final Duration lifetime;

    public RsaAccessTokenIssuer(
            @ConfigProperty(name = "mp.jwt.verify.issuer") String issuer,
            @ConfigProperty(name = "security.jwt.access-token-lifetime", defaultValue = "PT15M") Duration lifetime) {
        this.issuer = issuer;
        this.lifetime = lifetime;
    }

    @Override
    public IssuedAccessToken issue(Long userId, UserRole role) {
        long expiresInSeconds = lifetime.toSeconds();
        String token = Jwt.issuer(issuer)
                .subject(String.valueOf(userId))
                .claim("role", role.name())
                .expiresIn(lifetime)
                .sign();

        return new IssuedAccessToken(token, expiresInSeconds);
    }
}
