package com.victor.ecommerce.infrastructure.persistence.product;

import com.victor.ecommerce.domain.product.Product;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class ProductRepository implements PanacheRepository<Product> {

    public Optional<Product> findByName(String name) {
        return find("LOWER(name) = LOWER(?1)", name)
                .firstResultOptional();
    }
}