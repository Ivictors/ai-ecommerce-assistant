package com.victor.ecommerce.infrastructure.persistence.knowledge;

import com.victor.ecommerce.domain.knowledge.KnowledgeChunk;
import com.victor.ecommerce.domain.knowledge.KnowledgeDocumentVersion;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class KnowledgeChunkRepository implements PanacheRepository<KnowledgeChunk> {
    @Inject
    EntityManager entityManager;

    public long deleteForVersion(KnowledgeDocumentVersion version) {
        return delete("documentVersion", version);
    }

    public List<Object[]> searchSimilar(String vector, boolean authenticated, boolean admin) {
        return entityManager.createNativeQuery("""
                SELECT c.document_version_id, c.chunk_index, c.content, v.document_id
                FROM knowledge_chunks c
                JOIN knowledge_document_versions v ON v.id = c.document_version_id
                JOIN knowledge_documents d ON d.id = v.document_id
                WHERE v.state = 'READY' AND d.current_version_id = v.id
                  AND (
                    :admin = true
                    OR v.scope = 'PUBLIC'
                    OR (:authenticated = true AND v.scope = 'AUTHENTICATED_USER')
                  )
                ORDER BY c.embedding <=> CAST(:vector AS vector)
                LIMIT 5
                """)
                .setParameter("vector", vector)
                .setParameter("admin", admin)
                .setParameter("authenticated", authenticated)
                .getResultList();
    }
}
