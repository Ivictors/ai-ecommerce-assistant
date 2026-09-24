package com.victor.ecommerce.presentation.rest.chat;

import com.victor.ecommerce.application.chat.ChatApplicationService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/api/chat")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
@RolesAllowed("USER")
public class ChatResource {

    private final ChatApplicationService chatService;

    public ChatResource(ChatApplicationService chatService) {
        this.chatService = chatService;
    }

    @POST
    public String chat(
            @QueryParam("conversationId") Long conversationId,
            String message) {

        if (conversationId == null) {
            throw new BadRequestException("conversationId is required");
        }

        return chatService.chat(conversationId, message);
    }
}
