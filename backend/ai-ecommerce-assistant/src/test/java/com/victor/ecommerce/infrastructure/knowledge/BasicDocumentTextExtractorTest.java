package com.victor.ecommerce.infrastructure.knowledge;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BasicDocumentTextExtractorTest {
    @Test
    void extractsPlainText() {
        var extractor = new BasicDocumentTextExtractor();
        assertEquals("policy", extractor.extract("policy.txt", "text/plain", "policy".getBytes()));
    }
}
