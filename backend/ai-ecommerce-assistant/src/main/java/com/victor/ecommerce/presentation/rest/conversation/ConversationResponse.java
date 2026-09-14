package com.victor.ecommerce.presentation.rest.conversation;

import com.victor.ecommerce.domain.conversation.Conversation;

import java.time.Instant;

public record ConversationResponse(
        Long id,
        Long userId,
        Instant createdAt
) {

    public static ConversationResponse from(
            Conversation conversation
    ) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getUser().getId(),
                conversation.getCreatedAt()
        );
    }
}