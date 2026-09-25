package com.victor.ecommerce.presentation.rest.error;

import com.victor.ecommerce.application.chat.ConversationNotFoundException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotFoundExceptionMapper
        implements ExceptionMapper<NotFoundException> {

    @Override
    public Response toResponse(NotFoundException exception) {
        return notFound();
    }

    public static Response conversationNotFound(
            ConversationNotFoundException exception) {
        return notFound();
    }

    private static Response notFound() {
        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiErrorResponse(
                        "RESOURCE_NOT_FOUND",
                        "Resource not found"
                ))
                .build();
    }
}
