ALTER TABLE kb_source_document
    ADD COLUMN IF NOT EXISTS error_message TEXT NULL AFTER status,
    ADD COLUMN IF NOT EXISTS page_count INT NULL AFTER error_message,
    ADD COLUMN IF NOT EXISTS chunk_count INT NULL AFTER page_count;
