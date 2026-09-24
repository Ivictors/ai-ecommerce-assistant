package com.victor.ecommerce.presentation.rest.conversation;

import com.victor.ecommerce.application.conversation.ConversationService;
import com.victor.ecommerce.application.security.CurrentUserService;
import com.victor.ecommerce.domain.conversation.Conversation;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/conversations")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("USER")
public class ConversationResource {

    private final ConversationService conversationService;
    private final CurrentUserService currentUserService;

    public ConversationResource(
            ConversationService conversationService
            , CurrentUserService currentUserService
    ) {
        this.conversationService = conversationService;
        this.currentUserService = currentUserService;
    }

    @GET
    public List<ConversationResponse> findCurrentUserConversations() {
        return conversationService.findByUserId(currentUserService.getUserId())
                .stream()
                .map(ConversationResponse::from)
                .toList();
    }
}
