package com.victor.ecommerce.presentation.rest.knowledge;

import com.victor.ecommerce.application.knowledge.KnowledgeDocumentService;
import com.victor.ecommerce.application.knowledge.DocumentProcessingService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class KnowledgeDocumentResourceTest {
    @Test
    void resourceRequiresApplicationService() {
        assertNotNull(new KnowledgeDocumentResource(mock(KnowledgeDocumentService.class), mock(DocumentProcessingService.class)));
    }
}
