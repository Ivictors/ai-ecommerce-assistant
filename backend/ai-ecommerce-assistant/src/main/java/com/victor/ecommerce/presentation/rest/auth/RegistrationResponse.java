package com.victor.ecommerce.presentation.rest.auth;

import com.victor.ecommerce.application.account.UserRegistrationService;

public record RegistrationResponse(
        long id,
        String name,
        String email,
        String role
) {

    public static RegistrationResponse from(UserRegistrationService.RegistrationResult result) {
        return new RegistrationResponse(result.id(), result.name(), result.email(), result.role().name());
    }
}
