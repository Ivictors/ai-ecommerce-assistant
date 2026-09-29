package com.victor.ecommerce.infrastructure.ai.dto;

public record UserIntent(
        String intent,
        String productName,
        Long orderId
) {
    public UserIntent(String intent, String productName) {
        this(intent, productName, null);
    }
}
