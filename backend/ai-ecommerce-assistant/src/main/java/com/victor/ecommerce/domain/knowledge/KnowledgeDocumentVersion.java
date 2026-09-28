package com.victor.ecommerce.domain.knowledge;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "knowledge_document_versions")
public class KnowledgeDocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private KnowledgeDocument document;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(nullable = false)
    private String filename;

    @Column(name = "media_type", nullable = false)
    private String mediaType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KnowledgeScope scope;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KnowledgeDocumentState state;

    @Lob
    @Column(name = "source_content", nullable = false, columnDefinition = "BYTEA")
    private byte[] sourceContent;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected KnowledgeDocumentVersion() {
    }

    public KnowledgeDocumentVersion(
            KnowledgeDocument document,
            Integer versionNumber,
            String filename,
            String mediaType,
            KnowledgeScope scope,
            byte[] sourceContent
    ) {
        this.document = document;
        this.versionNumber = versionNumber;
        this.filename = filename;
        this.mediaType = mediaType;
        this.scope = scope;
        this.sourceContent = sourceContent.clone();
        this.state = KnowledgeDocumentState.DRAFT;
        this.createdAt = Instant.now();
    }

    public void startProcessing() {
        if (state != KnowledgeDocumentState.DRAFT && state != KnowledgeDocumentState.FAILED) {
            throw new IllegalStateException("Document cannot be processed from its current state");
        }
        state = KnowledgeDocumentState.PROCESSING;
        failureReason = null;
    }

    public void markReady() {
        if (state != KnowledgeDocumentState.PROCESSING) {
            throw new IllegalStateException("Document can only become ready while processing");
        }
        state = KnowledgeDocumentState.READY;
    }

    public void markFailed(String reason) {
        if (state != KnowledgeDocumentState.PROCESSING) {
            throw new IllegalStateException("Document can only fail while processing");
        }
        state = KnowledgeDocumentState.FAILED;
        failureReason = reason;
    }

    public void unpublish() {
        if (state != KnowledgeDocumentState.DRAFT && state != KnowledgeDocumentState.READY && state != KnowledgeDocumentState.FAILED) {
            throw new IllegalStateException("Document cannot be unpublished from its current state");
        }
        state = KnowledgeDocumentState.UNPUBLISHED;
    }

    public Long getId() { return id; }
    public KnowledgeDocument getDocument() { return document; }
    public Integer getVersionNumber() { return versionNumber; }
    public String getFilename() { return filename; }
    public String getMediaType() { return mediaType; }
    public KnowledgeScope getScope() { return scope; }
    public KnowledgeDocumentState getState() { return state; }
    public byte[] getSourceContent() { return sourceContent.clone(); }
    public String getFailureReason() { return failureReason; }
}
