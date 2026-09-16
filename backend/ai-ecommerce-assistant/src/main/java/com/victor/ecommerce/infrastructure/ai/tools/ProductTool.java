package com.victor.ecommerce.infrastructure.ai.tools;

import com.victor.ecommerce.application.product.ProductService;
import com.victor.ecommerce.domain.product.Product;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;

@ApplicationScoped
public class ProductTool {

    private final ProductService productService;

    public ProductTool(ProductService productService) {
        this.productService = productService;
    }

    @Tool("""
        Searches for an active product by its exact name.
        Returns the product name, description, current price, and current stock.
        Use this tool whenever the user asks about a product's price, stock,
        description, or availability.
        """)
    public ProductInfo findProductByName(String name) {

        return productService.findByName(name)
                .filter(Product::isActive)
                .map(product -> new ProductInfo(
                        true,
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        product.getStock(),
                        null
                ))
                .orElseGet(() -> ProductInfo.notFound(name));
    }

    public record ProductInfo(
            boolean found,
            Long id,
            String name,
            String description,
            BigDecimal price,
            Integer stock,
            String message
    ) {

        public static ProductInfo notFound(String productName) {
            return new ProductInfo(
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "Product not found: " + productName
            );
        }
    }
}