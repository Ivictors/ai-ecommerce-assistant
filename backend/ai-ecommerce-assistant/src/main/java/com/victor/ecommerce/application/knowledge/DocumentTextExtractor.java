package com.victor.ecommerce.application.knowledge;

public interface DocumentTextExtractor {
    String extract(String filename, String mediaType, byte[] content);
}
