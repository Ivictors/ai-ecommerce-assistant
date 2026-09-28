package com.victor.ecommerce.infrastructure.knowledge;

import com.victor.ecommerce.application.knowledge.EmbeddingPort;
import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Arrays;
import java.util.stream.Collectors;

@ApplicationScoped
public class LangChainEmbeddingPort implements EmbeddingPort {
    private final EmbeddingModel model;

    public LangChainEmbeddingPort(EmbeddingModel model) {
        this.model = model;
    }

    @Override
    public String embed(String text) {
        float[] vector = model.embed(text).content().vector();
        return "[" + Arrays.stream(toDouble(vector))
                .mapToObj(Double::toString)
                .collect(Collectors.joining(",")) + "]";
    }

    private double[] toDouble(float[] vector) {
        double[] result = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            result[i] = vector[i];
        }
        return result;
    }
}
