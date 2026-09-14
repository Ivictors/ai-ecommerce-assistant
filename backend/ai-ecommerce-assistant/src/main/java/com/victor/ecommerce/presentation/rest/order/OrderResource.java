package com.victor.ecommerce.presentation.rest.order;

import com.victor.ecommerce.application.order.OrderService;
import com.victor.ecommerce.domain.order.Order;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/orders")
@Produces(MediaType.APPLICATION_JSON)
public class OrderResource {

    private final OrderService orderService;

    public OrderResource(OrderService orderService) {
        this.orderService = orderService;
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Long id) {

        Order order = orderService.findById(id);

        if (order == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .build();
        }

        return Response.ok(OrderResponse.from(order))
                .build();
    }

    @GET
    @Path("/user/{userId}")
    public List<OrderResponse> findByUserId(
            @PathParam("userId") Long userId
    ) {
        return orderService.findByUserId(userId)
                .stream()
                .map(OrderResponse::from)
                .toList();
    }
}