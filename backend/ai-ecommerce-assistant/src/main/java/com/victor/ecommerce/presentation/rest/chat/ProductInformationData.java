package com.victor.ecommerce.presentation.rest.chat;

import com.victor.ecommerce.domain.product.Product;

import java.math.BigDecimal;

public record ProductInformationData(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        boolean active
) {
    public static ProductInformationData from(Product product) {
        return new ProductInformationData(product.getId(), product.getName(), product.getDescription(),
                product.getPrice(), product.getStock(), product.isActive());
    }
}
