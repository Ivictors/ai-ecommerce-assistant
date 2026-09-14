package com.victor.ecommerce.presentation.rest.order;

import com.victor.ecommerce.domain.order.Order;
import com.victor.ecommerce.domain.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        Long id,
        Long userId,
        OrderStatus status,
        BigDecimal total,
        Instant createdAt
) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getStatus(),
                order.getTotal(),
                order.getCreatedAt()
        );
    }
}