package com.victor.ecommerce.infrastructure.persistence.conversation;

import com.victor.ecommerce.domain.conversation.Conversation;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ConversationRepository
        implements PanacheRepository<Conversation> {

    public List<Conversation> findByUserId(Long userId) {
        return find("user.id", userId).list();
    }

    public Optional<Conversation> findByIdAndUserId(
            Long conversationId,
            Long userId) {

        return find(
                "id = ?1 and user.id = ?2",
                conversationId,
                userId
        ).firstResultOptional();
    }
}
