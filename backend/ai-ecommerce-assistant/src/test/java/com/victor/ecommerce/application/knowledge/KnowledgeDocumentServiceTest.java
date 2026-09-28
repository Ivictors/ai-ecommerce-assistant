package com.victor.ecommerce.application.knowledge;

import com.victor.ecommerce.domain.knowledge.*;
import com.victor.ecommerce.infrastructure.persistence.knowledge.KnowledgeDocumentRepository;
import com.victor.ecommerce.infrastructure.persistence.knowledge.KnowledgeDocumentVersionRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KnowledgeDocumentServiceTest {
    @Test
    void createsDraftForSupportedText() {
        var documents = mock(KnowledgeDocumentRepository.class);
        var versions = mock(KnowledgeDocumentVersionRepository.class);
        var service = new KnowledgeDocumentService(documents, versions);

        var version = service.create("policy.md", "text/markdown", KnowledgeScope.PUBLIC, "content".getBytes());

        assertEquals(KnowledgeDocumentState.DRAFT, version.getState());
        verify(documents).persist(any(KnowledgeDocument.class));
        verify(versions).persist(any(KnowledgeDocumentVersion.class));
    }

    @Test
    void rejectsUnsupportedDocument() {
        var service = new KnowledgeDocumentService(mock(KnowledgeDocumentRepository.class), mock(KnowledgeDocumentVersionRepository.class));
        assertThrows(InvalidDocumentException.class, () -> service.create("file.exe", "application/octet-stream", KnowledgeScope.PUBLIC, new byte[]{1}));
    }
}
