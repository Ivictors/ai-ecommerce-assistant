package com.victor.ecommerce.presentation.rest.knowledge;

import com.victor.ecommerce.domain.knowledge.KnowledgeDocumentVersion;

public record KnowledgeDocumentResponse(
        Long id,
        Long documentId,
        Integer version,
        String filename,
        String mediaType,
        String scope,
        String state
) {
    public static KnowledgeDocumentResponse from(KnowledgeDocumentVersion version) {
        return new KnowledgeDocumentResponse(
                version.getId(),
                version.getDocument().getId(),
                version.getVersionNumber(),
                version.getFilename(),
                version.getMediaType(),
                version.getScope().name(),
                version.getState().name()
        );
    }
}
