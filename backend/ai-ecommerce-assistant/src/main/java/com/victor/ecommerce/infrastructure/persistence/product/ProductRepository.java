package com.victor.ecommerce.infrastructure.persistence.product;

import com.victor.ecommerce.domain.product.Product;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProductRepository implements PanacheRepository<Product> {
}