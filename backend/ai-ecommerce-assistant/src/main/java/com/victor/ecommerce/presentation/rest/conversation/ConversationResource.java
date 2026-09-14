package com.victor.ecommerce.presentation.rest.conversation;

import com.victor.ecommerce.application.conversation.ConversationService;
import com.victor.ecommerce.domain.conversation.Conversation;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/conversations")
@Produces(MediaType.APPLICATION_JSON)
public class ConversationResource {

    private final ConversationService conversationService;

    public ConversationResource(
            ConversationService conversationService
    ) {
        this.conversationService = conversationService;
    }

    @GET
    @Path("/user/{userId}")
    public List<ConversationResponse> findByUserId(
            @PathParam("userId") Long userId
    ) {
        return conversationService.findByUserId(userId)
                .stream()
                .map(ConversationResponse::from)
                .toList();
    }
}