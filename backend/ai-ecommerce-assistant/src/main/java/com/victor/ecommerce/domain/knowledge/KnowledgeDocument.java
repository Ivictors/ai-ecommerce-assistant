package com.victor.ecommerce.domain.knowledge;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "knowledge_documents")
public class KnowledgeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "current_version_id")
    private Long currentVersionId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public KnowledgeDocument() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getCurrentVersionId() { return currentVersionId; }
    public void setCurrentVersionId(Long currentVersionId) { this.currentVersionId = currentVersionId; }
    public Instant getCreatedAt() { return createdAt; }
}
