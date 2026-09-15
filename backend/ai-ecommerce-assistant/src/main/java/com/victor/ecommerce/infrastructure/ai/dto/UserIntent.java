package com.victor.ecommerce.infrastructure.ai.dto;

public record UserIntent(
        String intent,
        String productName
) {
}