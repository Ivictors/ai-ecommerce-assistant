package com.victor.ecommerce.application.knowledge;

import com.victor.ecommerce.domain.knowledge.*;
import com.victor.ecommerce.infrastructure.persistence.knowledge.KnowledgeDocumentRepository;
import com.victor.ecommerce.infrastructure.persistence.knowledge.KnowledgeDocumentVersionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class KnowledgeDocumentService {
    private final KnowledgeDocumentRepository documents;
    private final KnowledgeDocumentVersionRepository versions;

    public KnowledgeDocumentService(KnowledgeDocumentRepository documents, KnowledgeDocumentVersionRepository versions) {
        this.documents = documents;
        this.versions = versions;
    }

    @Transactional
    public KnowledgeDocumentVersion create(String filename, String mediaType, KnowledgeScope scope, byte[] content) {
        if (filename == null || filename.isBlank() || mediaType == null || scope == null || content == null || content.length == 0) {
            throw new InvalidDocumentException("Document input is invalid");
        }
        if (!isSupported(mediaType, filename)) {
            throw new InvalidDocumentException("Document type is not supported");
        }
        KnowledgeDocument document = new KnowledgeDocument();
        documents.persist(document);
        KnowledgeDocumentVersion version = new KnowledgeDocumentVersion(document, 1, filename, mediaType, scope, content);
        versions.persist(version);
        document.setCurrentVersionId(version.getId());
        return version;
    }

    @Transactional
    public KnowledgeDocumentVersion unpublish(Long id) {
        KnowledgeDocumentVersion version = current(id);
        version.unpublish();
        return version;
    }

    @Transactional
    public void delete(Long id) {
        KnowledgeDocumentVersion version = current(id);
        if (version.getState() != KnowledgeDocumentState.UNPUBLISHED) {
            throw new InvalidDocumentStateException("Document must be unpublished before deletion");
        }
        versions.delete(version);
        documents.delete(version.getDocument());
    }

    public KnowledgeDocumentVersion current(Long id) {
        return versions.findCurrent(id).orElseThrow(DocumentNotFoundException::new);
    }

    private boolean isSupported(String mediaType, String filename) {
        String lower = filename.toLowerCase();
        return mediaType.equalsIgnoreCase("application/pdf")
                || mediaType.equalsIgnoreCase("text/plain")
                || mediaType.equalsIgnoreCase("text/markdown")
                || lower.endsWith(".pdf") || lower.endsWith(".txt") || lower.endsWith(".md");
    }
}
