package com.victor.ecommerce.infrastructure.ai;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService
@ApplicationScoped
public interface EcommerceAssistant {

    @UserMessage("""
        You are an AI assistant for an e-commerce application.
        Answer the user's question clearly and concisely.

        User question:
        {message}
        """)
    String chat(String message);
}