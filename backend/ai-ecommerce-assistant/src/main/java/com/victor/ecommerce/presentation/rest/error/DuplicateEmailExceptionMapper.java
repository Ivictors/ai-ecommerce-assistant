package com.victor.ecommerce.presentation.rest.error;

import com.victor.ecommerce.application.account.DuplicateEmailException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DuplicateEmailExceptionMapper implements ExceptionMapper<DuplicateEmailException> {

    @Override
    public Response toResponse(DuplicateEmailException exception) {
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiErrorResponse("EMAIL_ALREADY_REGISTERED", "Email is already registered"))
                .build();
    }
}
