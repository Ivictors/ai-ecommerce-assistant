package com.victor.ecommerce.application.chat;

import com.victor.ecommerce.domain.conversation.Conversation;
import com.victor.ecommerce.infrastructure.ai.EcommerceAssistant;
import com.victor.ecommerce.application.conversation.ConversationService;
import com.victor.ecommerce.application.security.CurrentUserService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ChatApplicationService {

    private final EcommerceAssistant assistant;
    private final ConversationService conversationService;
    private final CurrentUserService currentUserService;

    public ChatApplicationService(
            EcommerceAssistant assistant,
            ConversationService conversationService,
            CurrentUserService currentUserService) {
        this.assistant = assistant;
        this.conversationService = conversationService;
        this.currentUserService = currentUserService;
    }

    public String chat(Long conversationId, String message) {

        Conversation conversation = conversationService
                .findByIdForUser(
                        conversationId,
                        currentUserService.getUserId())
                .orElse(null);

        if (conversation == null) {
            throw new ConversationNotFoundException(conversationId);
        }

        return assistant.chat(
                conversation.getId().toString(),
                message
        );
    }
}
