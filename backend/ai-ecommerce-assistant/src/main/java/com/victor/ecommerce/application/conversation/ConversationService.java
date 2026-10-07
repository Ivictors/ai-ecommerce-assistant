package com.victor.ecommerce.application.conversation;

import com.victor.ecommerce.domain.conversation.Conversation;
import com.victor.ecommerce.domain.user.User;
import com.victor.ecommerce.application.security.UnauthenticatedUserException;
import com.victor.ecommerce.infrastructure.persistence.conversation.ConversationRepository;
import com.victor.ecommerce.infrastructure.persistence.user.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ConversationService {

    private final ConversationRepository repository;
    private final UserRepository userRepository;

    public ConversationService(
            ConversationRepository repository,
            UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Conversation createForUser(Long userId) {
        User user = userRepository.findByIdOptional(userId)
                .orElseThrow(UnauthenticatedUserException::new);
        Conversation conversation = new Conversation(user);
        repository.persist(conversation);
        return conversation;
    }

    public Conversation findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Conversation> findByIdForUser(
            Long conversationId,
            Long userId) {

        return repository.findByIdAndUserId(conversationId, userId);
    }

    public List<Conversation> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }
}
