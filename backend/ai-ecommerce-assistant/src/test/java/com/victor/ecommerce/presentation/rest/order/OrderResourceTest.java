package com.victor.ecommerce.presentation.rest.order;

import com.victor.ecommerce.application.order.OrderService;
import com.victor.ecommerce.application.security.CurrentUserService;
import com.victor.ecommerce.domain.order.Order;
import jakarta.annotation.security.RolesAllowed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderResourceTest {

    @Test
    void requiresUserRole() {
        RolesAllowed rolesAllowed = OrderResource.class.getAnnotation(RolesAllowed.class);

        assertNotNull(rolesAllowed);
        assertArrayEquals(new String[]{"USER"}, rolesAllowed.value());
    }

    @Test
    void findsOrderUsingAuthenticatedOwner() {
        OrderService service = mock(OrderService.class);
        CurrentUserService currentUser = mock(CurrentUserService.class);
        Order order = mock(Order.class);
        when(currentUser.getUserId()).thenReturn(7L);
        when(service.findByIdForUser(10L, 7L)).thenReturn(Optional.of(order));
        when(order.getUser()).thenReturn(mock(com.victor.ecommerce.domain.user.User.class));

        new OrderResource(service, currentUser).findById(10L);

        verify(service).findByIdForUser(10L, 7L);
    }

    @Test
    void listsOnlyCurrentUserOrders() {
        OrderService service = mock(OrderService.class);
        CurrentUserService currentUser = mock(CurrentUserService.class);
        when(currentUser.getUserId()).thenReturn(7L);
        when(service.findByUserId(7L)).thenReturn(List.of());

        assertEquals(List.of(), new OrderResource(service, currentUser).findCurrentUserOrders());
        verify(service).findByUserId(7L);
    }
}
