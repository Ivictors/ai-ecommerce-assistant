package com.victor.ecommerce.application.chat;

import com.victor.ecommerce.infrastructure.ai.dto.UserIntent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IntentValidatorTest {
    private final IntentValidator validator = new IntentValidator();

    @Test
    void acceptsSupportedProductIntent() {
        var result = validator.validate(new UserIntent("product_information", "Notebook"));
        assertEquals(IntentType.PRODUCT_INFORMATION, result.type());
        assertEquals("Notebook", result.productName());
    }

    @Test
    void convertsUnknownIntentToSafeFallback() {
        assertEquals(IntentType.UNKNOWN, validator.validate(new UserIntent("REFUND", null)).type());
        assertEquals(IntentType.UNKNOWN, validator.validate(null).type());
    }

    @Test
    void rejectsProductIntentWithoutProductName() {
        assertEquals(IntentType.UNKNOWN, validator.validate(new UserIntent("PRODUCT_INFORMATION", null)).type());
    }
}
