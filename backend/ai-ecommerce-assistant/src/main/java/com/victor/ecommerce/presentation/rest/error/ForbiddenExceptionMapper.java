package com.victor.ecommerce.presentation.rest.error;

import jakarta.ws.rs.ForbiddenException;
import com.victor.ecommerce.application.observability.ApplicationMetrics;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ForbiddenExceptionMapper
        implements ExceptionMapper<ForbiddenException> {

    private final ApplicationMetrics metrics;

    public ForbiddenExceptionMapper() { this.metrics = null; }

    @Inject
    public ForbiddenExceptionMapper(ApplicationMetrics metrics) { this.metrics = metrics; }

    @Override
    public Response toResponse(ForbiddenException exception) {
        if (metrics != null) metrics.recordAuthorizationDenial();
        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiErrorResponse(
                        "FORBIDDEN",
                        "You are not allowed to perform this operation"
                ))
                .build();
    }
}
