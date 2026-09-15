package com.victor.ecommerce.infrastructure.ai;

import com.victor.ecommerce.infrastructure.ai.dto.UserIntent;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.SystemMessage;

@RegisterAiService
@SystemMessage("""
    You classify the user's intention in an e-commerce application.

    Possible intents:
    - PRODUCT_INFORMATION
    - ORDER_STATUS
    - POLICY_INFORMATION
    - UNKNOWN

    Return the appropriate structured result.
    """)
public interface EcommerceAssistant {

    @UserMessage("{message}")
    String chat(String message);

    @UserMessage("{message}")
    UserIntent classify(String message);
}