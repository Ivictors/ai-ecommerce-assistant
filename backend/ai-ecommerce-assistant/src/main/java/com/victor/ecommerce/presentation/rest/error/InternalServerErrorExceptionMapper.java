package com.victor.ecommerce.presentation.rest.error;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.core.Context;
import org.jboss.logging.Logger;

import com.victor.ecommerce.presentation.rest.observability.RequestCorrelationFilter;

@Provider
public class InternalServerErrorExceptionMapper
        implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(InternalServerErrorExceptionMapper.class);

    @Context
    ContainerRequestContext requestContext;

    @Override
    public Response toResponse(Throwable exception) {
        LOG.errorf(
                "Unhandled request failure requestId=%s exceptionType=%s causeType=%s",
                requestId(),
                exceptionType(exception),
                causeType(exception));
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiErrorResponse(
                        "INTERNAL_ERROR",
                        "An unexpected error occurred"
                ))
                .build();
    }

    private String requestId() {
        return RequestCorrelationFilter.requestId(requestContext);
    }

    private static String exceptionType(Throwable exception) {
        return exception == null ? "unknown" : exception.getClass().getName();
    }

    private static String causeType(Throwable exception) {
        Throwable cause = exception == null ? null : exception.getCause();
        return cause == null ? "none" : cause.getClass().getName();
    }
}
