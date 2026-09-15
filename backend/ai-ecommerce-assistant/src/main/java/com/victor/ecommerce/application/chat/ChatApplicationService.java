package com.victor.ecommerce.application.chat;

import com.victor.ecommerce.domain.conversation.Conversation;
import com.victor.ecommerce.infrastructure.ai.EcommerceAssistant;
import com.victor.ecommerce.application.conversation.ConversationService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ChatApplicationService {

    private final EcommerceAssistant assistant;
    private final ConversationService conversationService;

    public ChatApplicationService(
            EcommerceAssistant assistant,
            ConversationService conversationService) {
        this.assistant = assistant;
        this.conversationService = conversationService;
    }

    public String chat(Long conversationId, String message) {

        Conversation conversation =
                conversationService.findById(conversationId);

        if (conversation == null) {
            throw new ConversationNotFoundException(conversationId);
        }

        return assistant.chat(
                conversation.getId().toString(),
                message
        );
    }
}