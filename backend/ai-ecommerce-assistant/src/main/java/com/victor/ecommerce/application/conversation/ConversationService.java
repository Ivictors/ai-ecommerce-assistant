package com.victor.ecommerce.application.conversation;

import com.victor.ecommerce.domain.conversation.Conversation;
import com.victor.ecommerce.infrastructure.persistence.conversation.ConversationRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ConversationService {

    private final ConversationRepository repository;

    public ConversationService(ConversationRepository repository) {
        this.repository = repository;
    }

    public Conversation findById(Long id) {
        return repository.findById(id);
    }

    public List<Conversation> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }
}