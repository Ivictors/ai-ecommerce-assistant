package com.victor.ecommerce.application.chat;

import com.victor.ecommerce.domain.conversation.Conversation;
import com.victor.ecommerce.infrastructure.ai.EcommerceAssistant;
import com.victor.ecommerce.application.conversation.ConversationService;
import com.victor.ecommerce.application.security.CurrentUserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.victor.ecommerce.application.knowledge.KnowledgeRetriever;
import com.victor.ecommerce.application.knowledge.RetrievedKnowledge;
import com.victor.ecommerce.application.order.OrderService;
import com.victor.ecommerce.application.product.ProductService;
import com.victor.ecommerce.presentation.rest.chat.*;

@ApplicationScoped
public class ChatApplicationService {

    private final EcommerceAssistant assistant;
    private final ConversationService conversationService;
    private final CurrentUserService currentUserService;
    private final KnowledgeRetriever knowledgeRetriever;
    private final IntentValidator intentValidator;
    private final ProductService productService;
    private final OrderService orderService;

    @Inject
    public ChatApplicationService(
            EcommerceAssistant assistant,
            ConversationService conversationService,
            CurrentUserService currentUserService,
            KnowledgeRetriever knowledgeRetriever,
            IntentValidator intentValidator,
            ProductService productService,
            OrderService orderService) {
        this.assistant = assistant;
        this.conversationService = conversationService;
        this.currentUserService = currentUserService;
        this.knowledgeRetriever = knowledgeRetriever;
        this.intentValidator = intentValidator;
        this.productService = productService;
        this.orderService = orderService;
    }

    public ChatApplicationService(EcommerceAssistant assistant, ConversationService conversationService,
                                  CurrentUserService currentUserService) {
        this(assistant, conversationService, currentUserService, message -> java.util.List.of());
    }

    public ChatApplicationService(EcommerceAssistant assistant, ConversationService conversationService,
                                  CurrentUserService currentUserService, KnowledgeRetriever knowledgeRetriever) {
        this(assistant, conversationService, currentUserService, knowledgeRetriever, new IntentValidator(), null, null);
    }

    public ChatResponse chat(Long conversationId, String message) {

        Conversation conversation = conversationService
                .findByIdForUser(
                        conversationId,
                        currentUserService.getUserId())
                .orElse(null);

        if (conversation == null) {
            throw new ConversationNotFoundException(conversationId);
        }

        var validatedIntent = intentValidator.validate(assistant.classify(message));
        if (validatedIntent.type() == IntentType.PRODUCT_INFORMATION && productService != null) {
            return productService.findByName(validatedIntent.productName())
                    .filter(product -> product.isActive())
                    .map(product -> ChatResponse.product("Product information", ProductInformationData.from(product)))
                    .orElseGet(() -> ChatResponse.fallback("I could not find that product."));
        }
        if (validatedIntent.type() == IntentType.ORDER_STATUS && orderService != null) {
            return orderService.findByIdForUser(validatedIntent.orderId(), currentUserService.getUserId())
                    .map(order -> ChatResponse.order("Order status", OrderStatusData.from(order)))
                    .orElseGet(() -> ChatResponse.fallback("I could not find that order."));
        }
        var knowledge = knowledgeRetriever.retrieve(message);
        if (validatedIntent.type() == IntentType.POLICY_INFORMATION) {
            if (knowledge.isEmpty()) {
                return ChatResponse.fallback("I do not have enough approved knowledge to answer that policy question.");
            }
            return ChatResponse.policy("Policy information", new PolicyInformationData(
                    knowledge.stream().map(RetrievedKnowledge::content).reduce((a, b) -> a + "\n" + b).orElse("")));
        }
        String groundedMessage = knowledge.isEmpty()
                ? "No approved knowledge context was found. Do not invent or assert company policy. If this is a policy question, explain that the available knowledge is insufficient.\n\nUser question:\n" + message
                : "Approved knowledge context:\n" + knowledge.stream().map(item -> item.content()).reduce((a, b) -> a + "\n" + b).orElse("") + "\n\nUser question:\n" + message;

        return ChatResponse.text(assistant.chat(
                conversation.getId().toString(),
                groundedMessage
        ));
    }
}
