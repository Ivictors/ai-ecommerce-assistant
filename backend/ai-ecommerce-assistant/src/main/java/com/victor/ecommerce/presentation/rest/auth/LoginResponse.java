package com.victor.ecommerce.presentation.rest.auth;

import com.victor.ecommerce.application.security.AccessTokenIssuer;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {

    public static LoginResponse from(AccessTokenIssuer.IssuedAccessToken token) {
        return new LoginResponse(token.value(), "Bearer", token.expiresInSeconds());
    }
}
