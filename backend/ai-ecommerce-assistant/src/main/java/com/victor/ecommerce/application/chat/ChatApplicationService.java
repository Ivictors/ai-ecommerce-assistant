package com.victor.ecommerce.application.chat;

import com.victor.ecommerce.domain.conversation.Conversation;
import com.victor.ecommerce.infrastructure.ai.EcommerceAssistant;
import com.victor.ecommerce.application.conversation.ConversationService;
import com.victor.ecommerce.application.security.CurrentUserService;
import jakarta.enterprise.context.ApplicationScoped;
import com.victor.ecommerce.application.knowledge.KnowledgeRetriever;

@ApplicationScoped
public class ChatApplicationService {

    private final EcommerceAssistant assistant;
    private final ConversationService conversationService;
    private final CurrentUserService currentUserService;
    private final KnowledgeRetriever knowledgeRetriever;

    public ChatApplicationService(
            EcommerceAssistant assistant,
            ConversationService conversationService,
            CurrentUserService currentUserService,
            KnowledgeRetriever knowledgeRetriever) {
        this.assistant = assistant;
        this.conversationService = conversationService;
        this.currentUserService = currentUserService;
        this.knowledgeRetriever = knowledgeRetriever;
    }

    public ChatApplicationService(EcommerceAssistant assistant, ConversationService conversationService,
                                  CurrentUserService currentUserService) {
        this(assistant, conversationService, currentUserService, message -> java.util.List.of());
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

        var knowledge = knowledgeRetriever.retrieve(message);
        String groundedMessage = knowledge.isEmpty()
                ? "No approved knowledge context was found. Do not invent or assert company policy. If this is a policy question, explain that the available knowledge is insufficient.\n\nUser question:\n" + message
                : "Approved knowledge context:\n" + knowledge.stream().map(item -> item.content()).reduce((a, b) -> a + "\n" + b).orElse("") + "\n\nUser question:\n" + message;

        return assistant.chat(
                conversation.getId().toString(),
                groundedMessage
        );
    }
}
