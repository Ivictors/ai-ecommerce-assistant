package com.victor.ecommerce.application.security;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CurrentUserService {

    private final SecurityIdentity securityIdentity;

    public CurrentUserService(SecurityIdentity securityIdentity) {
        this.securityIdentity = securityIdentity;
    }

    public Long getUserId() {

        if (securityIdentity.isAnonymous()) {
            throw new IllegalStateException(
                    "User is not authenticated"
            );
        }

        return Long.valueOf(
                securityIdentity.getPrincipal().getName()
        );
    }
}