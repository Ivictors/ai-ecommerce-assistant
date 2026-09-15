package com.victor.ecommerce.application.chat;

public class ConversationNotFoundException extends RuntimeException {

    public ConversationNotFoundException(Long conversationId) {
        super("Conversation not found: " + conversationId);
    }
}