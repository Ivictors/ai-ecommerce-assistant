package com.victor.ecommerce.presentation.rest.error;

import com.victor.ecommerce.application.chat.ConversationNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ConversationNotFoundExceptionMapper
        implements ExceptionMapper<ConversationNotFoundException> {

    @Override
    public Response toResponse(ConversationNotFoundException exception) {
        return NotFoundExceptionMapper.conversationNotFound(exception);
    }
}
