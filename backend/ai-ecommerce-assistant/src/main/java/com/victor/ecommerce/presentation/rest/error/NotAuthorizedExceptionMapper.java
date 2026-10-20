package com.victor.ecommerce.presentation.rest.error;

import jakarta.ws.rs.NotAuthorizedException;
import com.victor.ecommerce.application.observability.ApplicationMetrics;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotAuthorizedExceptionMapper
        implements ExceptionMapper<NotAuthorizedException> {

    private final ApplicationMetrics metrics;

    public NotAuthorizedExceptionMapper() { this.metrics = null; }

    @Inject
    public NotAuthorizedExceptionMapper(ApplicationMetrics metrics) { this.metrics = metrics; }

    @Override
    public Response toResponse(NotAuthorizedException exception) {
        if (metrics != null) metrics.recordAuthenticationFailure();
        return Response.status(Response.Status.UNAUTHORIZED)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiErrorResponse(
                        "UNAUTHENTICATED",
                        "Authentication is required"
                ))
                .build();
    }
}
