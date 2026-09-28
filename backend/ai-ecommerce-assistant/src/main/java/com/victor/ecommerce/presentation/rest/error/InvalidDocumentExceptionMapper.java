package com.victor.ecommerce.presentation.rest.error;

import com.victor.ecommerce.application.knowledge.InvalidDocumentException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class InvalidDocumentExceptionMapper implements ExceptionMapper<InvalidDocumentException> {
    public Response toResponse(InvalidDocumentException exception) {
        return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON)
                .entity(new ApiErrorResponse("INVALID_DOCUMENT", "Document input is invalid")).build();
    }
}
