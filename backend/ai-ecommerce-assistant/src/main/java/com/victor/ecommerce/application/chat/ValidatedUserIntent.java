package com.victor.ecommerce.application.chat;

public record ValidatedUserIntent(IntentType type, String productName, Long orderId) {
}
