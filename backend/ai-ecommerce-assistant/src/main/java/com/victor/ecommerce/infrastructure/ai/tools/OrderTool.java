package com.victor.ecommerce.infrastructure.ai.tools;

import com.victor.ecommerce.application.order.OrderService;
import com.victor.ecommerce.application.security.CurrentUserService;
import com.victor.ecommerce.domain.order.Order;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;

@ApplicationScoped
public class OrderTool {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    public OrderTool(
            OrderService orderService,
            CurrentUserService currentUserService) {

        this.orderService = orderService;
        this.currentUserService = currentUserService;
    }

    @Tool("""
        Retrieves the current status and total amount of an order by its ID.
        Use this tool when the user asks about the status of a specific order.
        """)
    public OrderInfo findOrderById(Long orderId) {

        Long userId = currentUserService.getUserId();

        return orderService
                .findByIdForUser(orderId, userId)
                .map(order -> new OrderInfo(
                        true,
                        order.getId(),
                        order.getStatus().name(),
                        order.getTotal(),
                        null
                ))
                .orElseGet(() ->
                        OrderInfo.notFound(orderId)
                );
    }

    public record OrderInfo(
            boolean found,
            Long orderId,
            String status,
            BigDecimal total,
            String message
    ) {

        public static OrderInfo notFound(Long orderId) {
            return new OrderInfo(
                    false,
                    null,
                    null,
                    null,
                    "Order not found: " + orderId
            );
        }
    }
}