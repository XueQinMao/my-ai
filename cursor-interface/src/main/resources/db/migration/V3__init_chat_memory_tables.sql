CREATE TABLE IF NOT EXISTS SPRING_AI_CHAT_MEMORY (
    conversation_id VARCHAR(64) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(10) NOT NULL,
    `timestamp` TIMESTAMP(3) NOT NULL,
    CONSTRAINT ck_spring_ai_chat_memory_type CHECK (type IN ('USER', 'ASSISTANT', 'SYSTEM', 'TOOL'))
);

CREATE INDEX idx_spring_ai_chat_memory_conversation_id_timestamp
    ON SPRING_AI_CHAT_MEMORY (conversation_id, `timestamp`);

CREATE TABLE IF NOT EXISTS chat_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    scene VARCHAR(32) NOT NULL,
    title VARCHAR(255) NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    last_message_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_chat_session_session_id (session_id),
    KEY idx_chat_session_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    role VARCHAR(16) NOT NULL,
    content LONGTEXT NOT NULL,
    message_index INT NOT NULL,
    tokens_input INT NULL,
    tokens_output INT NULL,
    trace_id VARCHAR(64) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_chat_message_session_id_created_at (session_id, created_at),
    KEY idx_chat_message_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS agent_memory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(64) NOT NULL,
    session_id VARCHAR(64) NULL,
    memory_type VARCHAR(32) NOT NULL,
    normalized_key VARCHAR(128) NULL,
    content TEXT NOT NULL,
    summary VARCHAR(512) NULL,
    importance DECIMAL(5,2) NOT NULL DEFAULT 0.50,
    confidence DECIMAL(5,2) NOT NULL DEFAULT 0.50,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    source_message_id BIGINT NULL,
    metadata_json JSON NULL,
    ttl_at DATETIME NULL,
    last_accessed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_agent_memory_user_status (user_id, status),
    KEY idx_agent_memory_session_id (session_id),
    KEY idx_agent_memory_normalized_key (normalized_key)
);

CREATE TABLE IF NOT EXISTS agent_memory_vector_ref (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    memory_id BIGINT NOT NULL,
    vector_doc_id VARCHAR(64) NOT NULL,
    embedding_model VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_agent_memory_vector_ref_memory_id
        FOREIGN KEY (memory_id) REFERENCES agent_memory (id)
        ON DELETE CASCADE,
    UNIQUE KEY uk_agent_memory_vector_ref_memory_id (memory_id),
    KEY idx_agent_memory_vector_ref_vector_doc_id (vector_doc_id)
);
