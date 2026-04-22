CREATE TABLE IF NOT EXISTS kb_source_document (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(512) NOT NULL,
    source_url TEXT NULL,
    source_org VARCHAR(128) NULL,
    doc_type VARCHAR(64) NULL,
    checksum VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'READY',
    published_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_kb_source_document_checksum (checksum)
);

CREATE TABLE IF NOT EXISTS kb_document_chunk (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_id BIGINT NOT NULL,
    chunk_no INT NOT NULL,
    chunk_text LONGTEXT NOT NULL,
    token_count INT NOT NULL DEFAULT 0,
    vector_doc_id VARCHAR(64) NOT NULL,
    metadata_json JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_kb_document_chunk_document_id
        FOREIGN KEY (document_id) REFERENCES kb_source_document (id)
        ON DELETE CASCADE
);

CREATE UNIQUE INDEX uk_kb_document_chunk_doc_chunk
    ON kb_document_chunk (document_id, chunk_no);

CREATE INDEX idx_kb_document_chunk_vector_doc_id
    ON kb_document_chunk (vector_doc_id);
