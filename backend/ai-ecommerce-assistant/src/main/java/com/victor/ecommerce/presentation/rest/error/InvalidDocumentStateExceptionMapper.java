package com.victor.ecommerce.presentation.rest.error;

import com.victor.ecommerce.application.knowledge.InvalidDocumentStateException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class InvalidDocumentStateExceptionMapper implements ExceptionMapper<InvalidDocumentStateException> {
    public Response toResponse(InvalidDocumentStateException exception) {
        return Response.status(Response.Status.CONFLICT).type(MediaType.APPLICATION_JSON)
                .entity(new ApiErrorResponse("INVALID_DOCUMENT_STATE", "Document state does not allow this operation")).build();
    }
}
