CREATE TABLE knowledge_chunks (
    id BIGSERIAL PRIMARY KEY,
    document_version_id BIGINT NOT NULL REFERENCES knowledge_document_versions(id),
    chunk_index INTEGER NOT NULL,
    content TEXT NOT NULL,
    embedding vector(1536) NOT NULL,
    CONSTRAINT uq_knowledge_chunk_index UNIQUE (document_version_id, chunk_index)
);
