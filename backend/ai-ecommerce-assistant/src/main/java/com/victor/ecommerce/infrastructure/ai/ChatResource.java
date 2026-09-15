package com.victor.ecommerce.infrastructure.ai;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/api/chat")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
public class ChatResource {

    @Inject
    EcommerceAssistant assistant;

    @POST
    public String chat(
            @QueryParam("conversationId") String conversationId,
            String message) {

        return assistant.chat(conversationId, message);
    }
}