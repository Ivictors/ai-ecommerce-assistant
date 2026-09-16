package com.victor.ecommerce.infrastructure.ai.tools;

import com.victor.ecommerce.application.order.OrderService;
import com.victor.ecommerce.domain.order.Order;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;

@ApplicationScoped
public class OrderTool {

    private final OrderService orderService;

    public OrderTool(OrderService orderService) {
        this.orderService = orderService;
    }

    @Tool("""
        Retrieves the current status and total amount of an order by its ID.
        Use this tool when the user asks about the status of a specific order.
        """)
    public OrderInfo findOrderById(Long orderId) {

        Order order = orderService.findById(orderId);

        if (order == null) {
            return OrderInfo.notFound(orderId);
        }

        return new OrderInfo(
                true,
                order.getId(),
                order.getStatus().name(),
                order.getTotal(),
                null
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