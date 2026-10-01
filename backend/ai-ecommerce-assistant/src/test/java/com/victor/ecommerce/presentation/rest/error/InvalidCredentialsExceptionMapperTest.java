package com.victor.ecommerce.presentation.rest.error;

import com.victor.ecommerce.application.security.InvalidCredentialsException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvalidCredentialsExceptionMapperTest {

    private final InvalidCredentialsExceptionMapper mapper = new InvalidCredentialsExceptionMapper();

    @Test
    void invalidCredentialsMustReturnUniformUnauthorizedResponse() {
        try (Response response = mapper.toResponse(new InvalidCredentialsException())) {
            ApiErrorResponse body = (ApiErrorResponse) response.getEntity();

            assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
            assertEquals("INVALID_CREDENTIALS", body.code());
            assertEquals("Invalid credentials", body.message());
        }
    }
}
