package com.victor.ecommerce.application.chat;

import com.victor.ecommerce.infrastructure.ai.dto.UserIntent;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Locale;

@ApplicationScoped
public class IntentValidator {
    public ValidatedUserIntent validate(UserIntent intent) {
        if (intent == null || intent.intent() == null || intent.intent().isBlank()) {
            return new ValidatedUserIntent(IntentType.UNKNOWN, null);
        }

        IntentType type;
        try {
            type = IntentType.valueOf(intent.intent().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            type = IntentType.UNKNOWN;
        }

        if (type == IntentType.PRODUCT_INFORMATION
                && (intent.productName() == null || intent.productName().isBlank())) {
            return new ValidatedUserIntent(IntentType.UNKNOWN, null);
        }

        return new ValidatedUserIntent(type, intent.productName());
    }
}
