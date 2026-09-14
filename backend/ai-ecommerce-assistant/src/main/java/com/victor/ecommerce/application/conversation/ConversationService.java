package com.victor.ecommerce.application.conversation;

import com.victor.ecommerce.domain.conversation.Conversation;
import com.victor.ecommerce.infrastructure.persistence.conversation.ConversationRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ConversationService {

    private final ConversationRepository conversationRepository;

    public ConversationService(
            ConversationRepository conversationRepository
    ) {
        this.conversationRepository = conversationRepository;
    }

    public List<Conversation> findByUserId(Long userId) {
        return conversationRepository.findByUserId(userId);
    }
}