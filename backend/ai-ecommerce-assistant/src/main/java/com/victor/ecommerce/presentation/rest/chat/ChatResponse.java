package com.victor.ecommerce.presentation.rest.chat;

public record ChatResponse(
        ChatResponseType type,
        String schemaVersion,
        String message,
        Object data
) {
    public static ChatResponse text(String message) {
        return new ChatResponse(ChatResponseType.TEXT, "1", message, null);
    }

    public static ChatResponse product(String message, ProductInformationData data) {
        return new ChatResponse(ChatResponseType.PRODUCT_INFORMATION, "1", message, data);
    }

    public static ChatResponse order(String message, OrderStatusData data) {
        return new ChatResponse(ChatResponseType.ORDER_STATUS, "1", message, data);
    }

    public static ChatResponse policy(String message, PolicyInformationData data) {
        return new ChatResponse(ChatResponseType.POLICY_INFORMATION, "1", message, data);
    }

    public static ChatResponse fallback(String message) {
        return new ChatResponse(ChatResponseType.FALLBACK, "1", message, null);
    }
}
