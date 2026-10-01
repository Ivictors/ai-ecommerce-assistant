package com.victor.ecommerce.application.security;

import com.victor.ecommerce.domain.user.UserRole;

public interface AccessTokenIssuer {

    IssuedAccessToken issue(Long userId, UserRole role);

    record IssuedAccessToken(String value, long expiresInSeconds) {
    }
}
