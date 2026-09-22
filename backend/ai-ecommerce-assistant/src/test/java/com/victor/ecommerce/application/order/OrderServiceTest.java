package com.victor.ecommerce.application.order;

import com.victor.ecommerce.domain.order.Order;
import com.victor.ecommerce.infrastructure.persistence.order.OrderRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    @Test
    void queriesOrdersUsingAuthenticatedOwner() {
        OrderRepository repository = mock(OrderRepository.class);
        OrderService service = new OrderService(repository);
        List<Order> orders = List.of(mock(Order.class));
        when(repository.findByUserId(7L)).thenReturn(orders);

        assertEquals(orders, service.findByUserId(7L));
        verify(repository).findByUserId(7L);
    }

    @Test
    void queriesSingleOrderUsingAuthenticatedOwner() {
        OrderRepository repository = mock(OrderRepository.class);
        OrderService service = new OrderService(repository);
        Order order = mock(Order.class);
        when(repository.findByIdAndUserId(10L, 7L)).thenReturn(Optional.of(order));

        assertEquals(Optional.of(order), service.findByIdForUser(10L, 7L));
        verify(repository).findByIdAndUserId(10L, 7L);
    }
}
