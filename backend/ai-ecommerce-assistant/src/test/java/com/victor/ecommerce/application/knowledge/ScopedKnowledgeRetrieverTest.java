package com.victor.ecommerce.application.knowledge;

import com.victor.ecommerce.domain.knowledge.*;
import com.victor.ecommerce.infrastructure.persistence.knowledge.KnowledgeChunkRepository;
import io.quarkus.security.identity.SecurityIdentity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ScopedKnowledgeRetrieverTest {
    @Test
    void excludesUnpublishedContent() {
        var identity = mock(SecurityIdentity.class);
        when(identity.isAnonymous()).thenReturn(false);
        var repository = mock(KnowledgeChunkRepository.class);
        var document = new KnowledgeDocument();
        var version = new KnowledgeDocumentVersion(document, 1, "policy.txt", "text/plain", KnowledgeScope.PUBLIC, "return policy".getBytes());
        version.startProcessing();
        version.markReady();
        document.setCurrentVersionId(version.getId());
        var chunk = new KnowledgeChunk(version, 0, "return policy", "[0]");
        when(repository.searchSimilar(anyString(), eq(true), eq(false)))
                .thenReturn(List.<Object[]>of(new Object[]{1L, 0, "return policy", 1L}));

        assertEquals(1, new ScopedKnowledgeRetriever(repository, identity, text -> "[0]").retrieve("return policy").size());
    }
}
