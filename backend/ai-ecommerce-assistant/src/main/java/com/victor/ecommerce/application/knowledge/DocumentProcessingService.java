package com.victor.ecommerce.application.knowledge;

import com.victor.ecommerce.domain.knowledge.KnowledgeChunk;
import com.victor.ecommerce.domain.knowledge.KnowledgeDocumentVersion;
import com.victor.ecommerce.infrastructure.persistence.knowledge.KnowledgeChunkRepository;
import com.victor.ecommerce.application.observability.ApplicationMetrics;
import com.victor.ecommerce.application.observability.ApplicationTracing;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class DocumentProcessingService {
    private final KnowledgeDocumentService documents;
    private final KnowledgeChunkRepository chunks;
    private final DocumentTextExtractor extractor;
    private final DocumentChunker chunker;
    private final EmbeddingPort embeddings;
    private final ApplicationMetrics metrics;
    private final ApplicationTracing tracing;

    public DocumentProcessingService(KnowledgeDocumentService documents, KnowledgeChunkRepository chunks,
                                     DocumentTextExtractor extractor, DocumentChunker chunker, EmbeddingPort embeddings) {
        this(documents, chunks, extractor, chunker, embeddings, null, null);
    }

    @Inject
    public DocumentProcessingService(KnowledgeDocumentService documents, KnowledgeChunkRepository chunks,
                                     DocumentTextExtractor extractor, DocumentChunker chunker, EmbeddingPort embeddings,
                                     ApplicationMetrics metrics,
                                     ApplicationTracing tracing) {
        this.documents = documents;
        this.chunks = chunks;
        this.extractor = extractor;
        this.chunker = chunker;
        this.embeddings = embeddings;
        this.metrics = metrics;
        this.tracing = tracing;
    }

    @Transactional
    public KnowledgeDocumentVersion process(Long documentId) {
        if (tracing == null) {
            return processInternal(documentId);
        }
        return tracing.trace("rag.document.processing", "rag-document-processing",
                () -> processInternal(documentId));
    }

    private KnowledgeDocumentVersion processInternal(Long documentId) {
        KnowledgeDocumentVersion version = documents.current(documentId);
        version.startProcessing();
        try {
            var text = extractor.extract(version.getFilename(), version.getMediaType(), version.getSourceContent());
            var parts = chunker.chunk(text);
            if (parts.isEmpty()) {
                throw new IllegalArgumentException("Document contains no chunks");
            }
            chunks.deleteForVersion(version);
            for (int i = 0; i < parts.size(); i++) {
                chunks.persist(new KnowledgeChunk(version, i, parts.get(i), embeddings.embed(parts.get(i))));
            }
            version.markReady();
            if (metrics != null) {
                metrics.recordRagProcessing("success");
            }
            return version;
        } catch (RuntimeException exception) {
            version.markFailed("Document processing failed");
            if (metrics != null) {
                metrics.recordRagProcessing("failure");
            }
            throw new DocumentProcessingException("The document could not be processed", exception);
        }
    }
}
