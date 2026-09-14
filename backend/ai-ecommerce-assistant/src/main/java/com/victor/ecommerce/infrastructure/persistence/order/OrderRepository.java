package com.victor.ecommerce.infrastructure.persistence.order;

import com.victor.ecommerce.domain.order.Order;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class OrderRepository implements PanacheRepository<Order> {

    public List<Order> findByUserId(Long userId) {
        return find("user.id", userId).list();
    }
}