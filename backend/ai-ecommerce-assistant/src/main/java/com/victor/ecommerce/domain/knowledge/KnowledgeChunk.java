package com.victor.ecommerce.domain.knowledge;

import jakarta.persistence.*;

@Entity
@Table(name = "knowledge_chunks")
public class KnowledgeChunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "document_version_id", nullable = false)
    private KnowledgeDocumentVersion documentVersion;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, columnDefinition = "vector(1536)")
    private String embedding;

    protected KnowledgeChunk() {
    }

    public KnowledgeChunk(KnowledgeDocumentVersion version, int index, String content, String embedding) {
        this.documentVersion = version;
        this.chunkIndex = index;
        this.content = content;
        this.embedding = embedding;
    }

    public KnowledgeDocumentVersion getDocumentVersion() { return documentVersion; }
    public Integer getChunkIndex() { return chunkIndex; }
    public String getContent() { return content; }
    public String getEmbedding() { return embedding; }
}
