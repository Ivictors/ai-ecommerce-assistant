package com.victor.ecommerce.infrastructure.persistence.knowledge;

import com.victor.ecommerce.domain.knowledge.KnowledgeChunk;
import com.victor.ecommerce.domain.knowledge.KnowledgeDocumentVersion;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class KnowledgeChunkRepository implements PanacheRepository<KnowledgeChunk> {
    public long deleteForVersion(KnowledgeDocumentVersion version) {
        return delete("documentVersion", version);
    }
}
