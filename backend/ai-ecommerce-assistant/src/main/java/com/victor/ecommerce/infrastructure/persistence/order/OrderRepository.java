package com.victor.ecommerce.infrastructure.persistence.order;

import com.victor.ecommerce.domain.order.Order;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class OrderRepository implements PanacheRepository<Order> {

    public List<Order> findByUserId(Long userId) {
        return find("user.id", userId).list();
    }

    public Optional<Order> findByIdAndUserId(
            Long orderId,
            Long userId) {

        return find(
                "id = ?1 and user.id = ?2",
                orderId,
                userId
        ).firstResultOptional();
    }
}