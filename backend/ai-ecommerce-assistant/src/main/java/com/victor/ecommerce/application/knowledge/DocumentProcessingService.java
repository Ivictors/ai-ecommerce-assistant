package com.victor.ecommerce.application.knowledge;

import com.victor.ecommerce.domain.knowledge.KnowledgeChunk;
import com.victor.ecommerce.domain.knowledge.KnowledgeDocumentVersion;
import com.victor.ecommerce.infrastructure.persistence.knowledge.KnowledgeChunkRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class DocumentProcessingService {
    private final KnowledgeDocumentService documents;
    private final KnowledgeChunkRepository chunks;
    private final DocumentTextExtractor extractor;
    private final DocumentChunker chunker;
    private final EmbeddingPort embeddings;

    public DocumentProcessingService(KnowledgeDocumentService documents, KnowledgeChunkRepository chunks,
                                     DocumentTextExtractor extractor, DocumentChunker chunker, EmbeddingPort embeddings) {
        this.documents = documents;
        this.chunks = chunks;
        this.extractor = extractor;
        this.chunker = chunker;
        this.embeddings = embeddings;
    }

    @Transactional
    public KnowledgeDocumentVersion process(Long documentId) {
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
            return version;
        } catch (RuntimeException exception) {
            version.markFailed("Document processing failed");
            throw new DocumentProcessingException("The document could not be processed", exception);
        }
    }
}
