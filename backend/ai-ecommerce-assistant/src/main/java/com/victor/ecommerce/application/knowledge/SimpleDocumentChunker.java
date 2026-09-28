package com.victor.ecommerce.application.knowledge;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class SimpleDocumentChunker implements DocumentChunker {
    private static final int CHUNK_SIZE = 1000;

    @Override
    public List<String> chunk(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<String> chunks = new ArrayList<>();
        for (int start = 0; start < text.length(); start += CHUNK_SIZE) {
            chunks.add(text.substring(start, Math.min(start + CHUNK_SIZE, text.length())).trim());
        }
        return chunks.stream().filter(chunk -> !chunk.isBlank()).toList();
    }
}
