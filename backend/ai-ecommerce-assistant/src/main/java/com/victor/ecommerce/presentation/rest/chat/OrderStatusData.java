package com.victor.ecommerce.presentation.rest.chat;

import com.victor.ecommerce.domain.order.Order;
import com.victor.ecommerce.domain.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderStatusData(Long id, OrderStatus status, BigDecimal total, Instant createdAt) {
    public static OrderStatusData from(Order order) {
        return new OrderStatusData(order.getId(), order.getStatus(), order.getTotal(), order.getCreatedAt());
    }
}
