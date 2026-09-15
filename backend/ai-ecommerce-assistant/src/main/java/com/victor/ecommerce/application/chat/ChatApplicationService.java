package com.victor.ecommerce.application.chat;

import com.victor.ecommerce.infrastructure.ai.EcommerceAssistant;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ChatApplicationService {

    private final EcommerceAssistant assistant;

    public ChatApplicationService(EcommerceAssistant assistant) {
        this.assistant = assistant;
    }

    public String chat(String message) {
        try {
            return assistant.chat(message);
        } catch (RuntimeException exception) {
            throw new AiServiceException(
                    "Unable to process AI request.",
                    exception
            );
        }
    }
}