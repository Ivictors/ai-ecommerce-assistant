package com.victor.ecommerce.presentation.rest.error;

import com.victor.ecommerce.application.security.InvalidCredentialsException;
import com.victor.ecommerce.application.observability.ApplicationMetrics;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class InvalidCredentialsExceptionMapper implements ExceptionMapper<InvalidCredentialsException> {

    private final ApplicationMetrics metrics;

    public InvalidCredentialsExceptionMapper() {
        this.metrics = null;
    }

    @Inject
    public InvalidCredentialsExceptionMapper(ApplicationMetrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public Response toResponse(InvalidCredentialsException exception) {
        if (metrics != null) metrics.recordAuthenticationFailure();
        return Response.status(Response.Status.UNAUTHORIZED)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiErrorResponse("INVALID_CREDENTIALS", "Invalid credentials"))
                .build();
    }
}
