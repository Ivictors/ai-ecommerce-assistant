package com.victor.ecommerce.application.knowledge;

import com.victor.ecommerce.domain.knowledge.KnowledgeChunk;
import com.victor.ecommerce.domain.knowledge.KnowledgeDocumentState;
import com.victor.ecommerce.domain.knowledge.KnowledgeScope;
import com.victor.ecommerce.infrastructure.persistence.knowledge.KnowledgeChunkRepository;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class ScopedKnowledgeRetriever implements KnowledgeRetriever {
    private final KnowledgeChunkRepository chunks;
    private final SecurityIdentity identity;

    public ScopedKnowledgeRetriever(KnowledgeChunkRepository chunks, SecurityIdentity identity) {
        this.chunks = chunks;
        this.identity = identity;
    }

    @Override
    public List<RetrievedKnowledge> retrieve(String question) {
        if (question == null || question.isBlank()) {
            return List.of();
        }
        boolean authenticated = !identity.isAnonymous();
        boolean admin = identity.hasRole("ADMIN");
        String normalizedQuestion = question.toLowerCase(Locale.ROOT);
        return chunks.listAll().stream()
                .filter(chunk -> isEligible(chunk, authenticated, admin))
                .filter(chunk -> containsQuestionTerm(chunk, normalizedQuestion))
                .limit(5)
                .map(this::toRetrievedKnowledge)
                .toList();
    }

    private boolean isEligible(KnowledgeChunk chunk, boolean authenticated, boolean admin) {
        var version = chunk.getDocumentVersion();
        if (version.getState() != KnowledgeDocumentState.READY
                || (version.getDocument().getCurrentVersionId() != null
                && !version.getId().equals(version.getDocument().getCurrentVersionId()))) {
            return false;
        }
        return version.getScope() == KnowledgeScope.PUBLIC
                || (version.getScope() == KnowledgeScope.AUTHENTICATED_USER && authenticated)
                || (version.getScope() == KnowledgeScope.ADMIN && admin);
    }

    private boolean containsQuestionTerm(KnowledgeChunk chunk, String question) {
        return Arrays.stream(question.split("\\W+"))
                .filter(term -> term.length() > 2)
                .anyMatch(term -> chunk.getContent().toLowerCase(Locale.ROOT).contains(term));
    }

    private RetrievedKnowledge toRetrievedKnowledge(KnowledgeChunk chunk) {
        var version = chunk.getDocumentVersion();
        return new RetrievedKnowledge(version.getDocument().getId(), version.getId(), chunk.getChunkIndex(), chunk.getContent());
    }
}
