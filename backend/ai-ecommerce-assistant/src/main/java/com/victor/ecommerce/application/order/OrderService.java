package com.victor.ecommerce.application.order;

import com.victor.ecommerce.domain.order.Order;
import com.victor.ecommerce.infrastructure.persistence.order.OrderRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order findById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}