package com.victor.ecommerce.infrastructure.persistence.conversation;

import com.victor.ecommerce.domain.conversation.Conversation;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ConversationRepository
        implements PanacheRepository<Conversation> {

    public List<Conversation> findByUserId(Long userId) {
        return find("user.id", userId).list();
    }
}