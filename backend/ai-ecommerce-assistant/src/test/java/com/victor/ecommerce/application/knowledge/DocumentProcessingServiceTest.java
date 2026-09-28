package com.victor.ecommerce.application.knowledge;

import com.victor.ecommerce.domain.knowledge.*;
import com.victor.ecommerce.infrastructure.persistence.knowledge.KnowledgeChunkRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DocumentProcessingServiceTest {
    @Test
    void processesTextIntoReadyDocument() {
        var document = mock(KnowledgeDocumentService.class);
        var version = new KnowledgeDocumentVersion(new KnowledgeDocument(), 1, "a.txt", "text/plain", KnowledgeScope.PUBLIC, "text".getBytes());
        var chunks = mock(KnowledgeChunkRepository.class);
        var service = new DocumentProcessingService(document, chunks, (f, m, c) -> "text", text -> java.util.List.of(text), text -> "[0,0,0]");
        when(document.current(1L)).thenReturn(version);

        assertEquals(KnowledgeDocumentState.READY, service.process(1L).getState());
        verify(chunks).persist(any(KnowledgeChunk.class));
    }

    @Test
    void marksFailedWhenExtractionFails() {
        var document = mock(KnowledgeDocumentService.class);
        var version = new KnowledgeDocumentVersion(new KnowledgeDocument(), 1, "a.txt", "text/plain", KnowledgeScope.PUBLIC, "text".getBytes());
        when(document.current(1L)).thenReturn(version);
        var service = new DocumentProcessingService(document, mock(KnowledgeChunkRepository.class), (f, m, c) -> { throw new IllegalArgumentException(); }, text -> java.util.List.of(text), text -> "[0,0,0]");

        assertThrows(DocumentProcessingException.class, () -> service.process(1L));
        assertEquals(KnowledgeDocumentState.FAILED, version.getState());
    }
}
