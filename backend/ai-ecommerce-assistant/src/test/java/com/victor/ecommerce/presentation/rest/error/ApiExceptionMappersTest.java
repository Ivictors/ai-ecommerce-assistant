package com.victor.ecommerce.presentation.rest.error;

import com.victor.ecommerce.application.chat.ConversationNotFoundException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ApiExceptionMappersTest {

    @Test
    void mapsBadRequest() {
        var response = new BadRequestExceptionMapper()
                .toResponse(new BadRequestException("conversationId is required"));

        assertError(response, 400, "INVALID_REQUEST", "conversationId is required");
    }

    @Test
    void mapsAuthenticationFailureWithoutTokenDetails() {
        var response = new NotAuthorizedExceptionMapper()
                .toResponse(new NotAuthorizedException("Bearer secret-token"));

        assertError(response, 401, "UNAUTHENTICATED", "Authentication is required");
        assertFalse(response.getEntity().toString().contains("secret-token"));
    }

    @Test
    void mapsForbidden() {
        var response = new ForbiddenExceptionMapper()
                .toResponse(new ForbiddenException());

        assertError(response, 403, "FORBIDDEN", "You are not allowed to perform this operation");
    }

    @Test
    void mapsPrivateResourceNotFound() {
        var response = new ConversationNotFoundExceptionMapper()
                .toResponse(new ConversationNotFoundException(10L));

        assertError(response, 404, "RESOURCE_NOT_FOUND", "Resource not found");
    }

    @Test
    void mapsJaxRsNotFound() {
        var response = new NotFoundExceptionMapper()
                .toResponse(new NotFoundException());

        assertError(response, 404, "RESOURCE_NOT_FOUND", "Resource not found");
    }

    @Test
    void hidesUnexpectedExceptionDetails() {
        var response = new InternalServerErrorExceptionMapper()
                .toResponse(new IllegalStateException("database password leaked"));

        assertError(response, 500, "INTERNAL_ERROR", "An unexpected error occurred");
        assertFalse(response.getEntity().toString().contains("database password"));
    }

    private void assertError(
            jakarta.ws.rs.core.Response response,
            int status,
            String code,
            String message) {

        assertEquals(status, response.getStatus());
        ApiErrorResponse error = (ApiErrorResponse) response.getEntity();
        assertEquals(code, error.code());
        assertEquals(message, error.message());
    }
}
