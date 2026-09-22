package com.victor.ecommerce.infrastructure.ai.tools;

import com.victor.ecommerce.application.order.OrderService;
import com.victor.ecommerce.application.security.CurrentUserService;
import com.victor.ecommerce.domain.order.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderToolTest {

    @Test
    void returnsOnlyOrderOwnedByCurrentUser() {
        OrderService service = mock(OrderService.class);
        CurrentUserService currentUser = mock(CurrentUserService.class);
        Order order = mock(Order.class);
        when(currentUser.getUserId()).thenReturn(7L);
        when(service.findByIdForUser(10L, 7L)).thenReturn(Optional.of(order));
        when(order.getId()).thenReturn(10L);
        when(order.getStatus()).thenReturn(com.victor.ecommerce.domain.order.OrderStatus.PAID);
        when(order.getTotal()).thenReturn(new BigDecimal("19.90"));

        OrderTool.OrderInfo result = new OrderTool(service, currentUser).findOrderById(10L);

        assertTrue(result.found());
        assertEquals(10L, result.orderId());
        assertEquals("PAID", result.status());
        assertEquals(new BigDecimal("19.90"), result.total());
    }

    @Test
    void hidesOrderThatDoesNotBelongToCurrentUser() {
        OrderService service = mock(OrderService.class);
        CurrentUserService currentUser = mock(CurrentUserService.class);
        when(currentUser.getUserId()).thenReturn(7L);
        when(service.findByIdForUser(10L, 7L)).thenReturn(Optional.empty());

        OrderTool.OrderInfo result = new OrderTool(service, currentUser).findOrderById(10L);

        assertFalse(result.found());
        assertEquals("Order not found: 10", result.message());
    }
}
