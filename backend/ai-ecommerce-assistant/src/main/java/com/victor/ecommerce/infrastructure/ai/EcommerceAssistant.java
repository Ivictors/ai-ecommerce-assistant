package com.victor.ecommerce.infrastructure.ai;

import com.victor.ecommerce.infrastructure.ai.dto.UserIntent;
import com.victor.ecommerce.infrastructure.ai.tools.OrderTool;
import com.victor.ecommerce.infrastructure.ai.tools.ProductTool;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;

@RegisterAiService
@SystemMessage("""
        You are the AI assistant for an e-commerce application.

        Your role is to help customers understand products, orders,
        and company policies.

        Communication guidelines:
        - Be clear and concise.
        - Use natural language.
        - Answer in the same language used by the customer.
        - Ask for clarification when the customer's request is ambiguous.
        - Do not provide information that you are not confident about.

        Data integrity rules:
        - Never invent product prices.
        - Never invent product stock levels.
        - Never invent order information.
        - Never invent payment information.
        - Never invent company policies.
        - Never claim that an action was completed unless the application
          explicitly confirms that it was completed.

        Important:
        Transactional information must come from the application.
        Company policies must come from the application's available
        knowledge sources.

        If the required information is not available, clearly state
        that you do not have enough information to answer.
        """)
public interface EcommerceAssistant {

    @ToolBox({
            ProductTool.class,
            OrderTool.class
    })
    @UserMessage("{message}")
    String chat(@MemoryId String memoryId, String message);

    @UserMessage("""
            Classify the user's intention.

            Possible intents:
            - PRODUCT_INFORMATION
            - ORDER_STATUS
            - POLICY_INFORMATION
            - UNKNOWN

            Return the appropriate structured result.

            User message:
            {message}
            """)
    UserIntent classify(String message);
}