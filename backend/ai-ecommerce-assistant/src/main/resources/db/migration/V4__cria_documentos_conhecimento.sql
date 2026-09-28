CREATE TABLE knowledge_documents (
    id BIGSERIAL PRIMARY KEY,
    current_version_id BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE knowledge_document_versions (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES knowledge_documents(id),
    version_number INTEGER NOT NULL,
    filename VARCHAR(255) NOT NULL,
    media_type VARCHAR(150) NOT NULL,
    scope VARCHAR(30) NOT NULL,
    state VARCHAR(30) NOT NULL,
    source_content BYTEA NOT NULL,
    failure_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_knowledge_document_version UNIQUE (document_id, version_number),
    CONSTRAINT chk_knowledge_scope CHECK (scope IN ('PUBLIC', 'AUTHENTICATED_USER', 'ADMIN')),
    CONSTRAINT chk_knowledge_state CHECK (state IN ('DRAFT', 'PROCESSING', 'READY', 'FAILED', 'UNPUBLISHED'))
);

ALTER TABLE knowledge_documents
    ADD CONSTRAINT fk_knowledge_current_version
    FOREIGN KEY (current_version_id) REFERENCES knowledge_document_versions(id);

CREATE INDEX idx_knowledge_versions_current_state
    ON knowledge_document_versions(document_id, state, version_number);
