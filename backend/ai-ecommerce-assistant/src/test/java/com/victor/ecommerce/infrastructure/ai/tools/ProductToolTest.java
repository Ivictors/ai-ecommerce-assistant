package com.victor.ecommerce.infrastructure.ai.tools;

import com.victor.ecommerce.application.product.ProductService;
import com.victor.ecommerce.domain.product.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductToolTest {

    @Test
    void retrievesAuthoritativeProductDataThroughApplicationService() {
        ProductService service = mock(ProductService.class);
        Product product = mock(Product.class);
        when(service.findByName("Keyboard")).thenReturn(Optional.of(product));
        when(product.isActive()).thenReturn(true);
        when(product.getId()).thenReturn(3L);
        when(product.getName()).thenReturn("Keyboard");
        when(product.getDescription()).thenReturn("Mechanical keyboard");
        when(product.getPrice()).thenReturn(new BigDecimal("99.90"));
        when(product.getStock()).thenReturn(4);

        ProductTool.ProductInfo result = new ProductTool(service)
                .findProductByName("Keyboard");

        assertTrue(result.found());
        assertEquals(3L, result.id());
        assertEquals(new BigDecimal("99.90"), result.price());
        assertEquals(4, result.stock());
        verify(service).findByName("Keyboard");
    }

    @Test
    void doesNotExposeInactiveProductThroughTool() {
        ProductService service = mock(ProductService.class);
        Product product = mock(Product.class);
        when(service.findByName("Keyboard")).thenReturn(Optional.of(product));
        when(product.isActive()).thenReturn(false);

        ProductTool.ProductInfo result = new ProductTool(service)
                .findProductByName("Keyboard");

        assertFalse(result.found());
        assertEquals("Product not found: Keyboard", result.message());
    }
}
