package com.victor.ecommerce.infrastructure.persistence.knowledge;

import com.victor.ecommerce.domain.knowledge.KnowledgeDocument;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class KnowledgeDocumentRepository implements PanacheRepository<KnowledgeDocument> {
}
