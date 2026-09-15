package com.victor.ecommerce.presentation.rest.chat;

import com.victor.ecommerce.application.chat.ChatApplicationService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/chat")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
public class ChatResource {

    private final ChatApplicationService chatService;

    public ChatResource(ChatApplicationService chatService) {
        this.chatService = chatService;
    }

    @POST
    public String chat(String message) {
        return chatService.chat(message);
    }
}