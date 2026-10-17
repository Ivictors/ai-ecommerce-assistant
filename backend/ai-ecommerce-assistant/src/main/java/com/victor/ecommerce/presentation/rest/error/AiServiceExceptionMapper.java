package com.victor.ecommerce.presentation.rest.error;

import com.victor.ecommerce.application.chat.AiServiceException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AiServiceExceptionMapper implements ExceptionMapper<AiServiceException> {

    @Override
    public Response toResponse(AiServiceException exception) {
        return Response.status(Response.Status.BAD_GATEWAY)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiErrorResponse(
                        "AI_PROVIDER_ERROR",
                        "The AI provider could not process the request"
                ))
                .build();
    }
}
