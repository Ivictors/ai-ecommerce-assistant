package com.victor.ecommerce.application.product;

import com.victor.ecommerce.domain.product.Product;
import com.victor.ecommerce.infrastructure.persistence.product.ProductRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.listAll();
    }

    public Product findById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> findByName(String name) {
        return productRepository.findByName(name);
    }

    @Transactional
    public Product create(
            String name,
            String description,
            BigDecimal price,
            Integer stock
    ) {
        Product product = new Product(
                name,
                description,
                price,
                stock
        );

        productRepository.persist(product);

        return product;
    }

    @Transactional
    public boolean delete(Long id) {
        return productRepository.deleteById(id);
    }
}