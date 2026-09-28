package com.victor.ecommerce.infrastructure.persistence.knowledge;

import com.victor.ecommerce.domain.knowledge.KnowledgeDocumentVersion;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class KnowledgeDocumentVersionRepository implements PanacheRepository<KnowledgeDocumentVersion> {
    public Optional<KnowledgeDocumentVersion> findCurrent(Long documentId) {
        return find("document.id = ?1 order by versionNumber desc", documentId).firstResultOptional();
    }
}
