package com.victor.ecommerce.application.knowledge;

import com.victor.ecommerce.infrastructure.persistence.knowledge.KnowledgeChunkRepository;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ScopedKnowledgeRetriever implements KnowledgeRetriever {
    private final KnowledgeChunkRepository chunks;
    private final SecurityIdentity identity;
    private final EmbeddingPort embeddings;

    public ScopedKnowledgeRetriever(KnowledgeChunkRepository chunks, SecurityIdentity identity, EmbeddingPort embeddings) {
        this.chunks = chunks;
        this.identity = identity;
        this.embeddings = embeddings;
    }

    @Override
    public List<RetrievedKnowledge> retrieve(String question) {
        if (question == null || question.isBlank()) {
            return List.of();
        }
        boolean authenticated = !identity.isAnonymous();
        boolean admin = identity.hasRole("ADMIN");
        return chunks.searchSimilar(embeddings.embed(question), authenticated, admin).stream()
                .map(row -> new RetrievedKnowledge(((Number) row[3]).longValue(), ((Number) row[0]).longValue(),
                        ((Number) row[1]).intValue(), (String) row[2]))
                .toList();
    }
}
