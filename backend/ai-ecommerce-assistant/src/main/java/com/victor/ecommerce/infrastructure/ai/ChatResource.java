package com.victor.ecommerce.infrastructure.ai;

import com.victor.ecommerce.infrastructure.ai.EcommerceAssistant;
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
    public String chat(String message) {
        return assistant.chat(message);
    }
}