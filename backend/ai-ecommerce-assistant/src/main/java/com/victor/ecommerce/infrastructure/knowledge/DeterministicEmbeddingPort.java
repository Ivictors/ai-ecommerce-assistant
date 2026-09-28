package com.victor.ecommerce.infrastructure.knowledge;

import com.victor.ecommerce.application.knowledge.EmbeddingPort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DeterministicEmbeddingPort implements EmbeddingPort {
    @Override
    public String embed(String text) {
        return "[" + "0,".repeat(1535) + "0]";
    }
}
