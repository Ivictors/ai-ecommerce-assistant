package com.victor.ecommerce.presentation.rest.error;

import com.victor.ecommerce.application.knowledge.DocumentProcessingException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DocumentProcessingExceptionMapper implements ExceptionMapper<DocumentProcessingException> {
    public Response toResponse(DocumentProcessingException exception) {
        return Response.status(422).type(MediaType.APPLICATION_JSON)
                .entity(new ApiErrorResponse("DOCUMENT_PROCESSING_FAILED", "The document could not be processed")).build();
    }
}
