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

        if (securityIdentity.isAnonymous()
                || securityIdentity.getPrincipal() == null) {
            throw new UnauthenticatedUserException();
        }

        String principalName = securityIdentity.getPrincipal().getName();

        if (principalName == null || principalName.isBlank()) {
            throw new UnauthenticatedUserException();
        }

        try {
            return Long.valueOf(principalName);
        } catch (NumberFormatException exception) {
            throw new UnauthenticatedUserException();
        }
    }
}
