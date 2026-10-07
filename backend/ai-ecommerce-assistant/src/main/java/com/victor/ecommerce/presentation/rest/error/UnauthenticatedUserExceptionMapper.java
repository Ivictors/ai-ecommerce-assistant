package com.victor.ecommerce.presentation.rest.error;

import com.victor.ecommerce.application.security.UnauthenticatedUserException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class UnauthenticatedUserExceptionMapper implements ExceptionMapper<UnauthenticatedUserException> {

    @Override
    public Response toResponse(UnauthenticatedUserException exception) {
        return Response.status(Response.Status.UNAUTHORIZED)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiErrorResponse("UNAUTHENTICATED", "Authentication is required"))
                .build();
    }
}
