package com.victor.ecommerce.presentation.rest.order;

import com.victor.ecommerce.application.order.OrderService;
import com.victor.ecommerce.application.security.CurrentUserService;
import com.victor.ecommerce.domain.order.Order;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/orders")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("USER")
public class OrderResource {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    public OrderResource(
            OrderService orderService,
            CurrentUserService currentUserService) {
        this.orderService = orderService;
        this.currentUserService = currentUserService;
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Long id) {

        Order order = orderService
                .findByIdForUser(id, currentUserService.getUserId())
                .orElse(null);

        if (order == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        }

        return Response.ok(OrderResponse.from(order))
                .build();
    }

    @GET
    public List<OrderResponse> findCurrentUserOrders() {
        return orderService.findByUserId(currentUserService.getUserId())
                .stream()
                .map(OrderResponse::from)
                .toList();
    }
}
