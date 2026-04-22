package com.my.ai.cursor.knowledge.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("kb_document_chunk")
public class KbDocumentChunk {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("document_id")
    private Long documentId;

    @TableField("chunk_no")
    private Integer chunkNo;

    @TableField("chunk_text")
    private String chunkText;

    @TableField("token_count")
    private Integer tokenCount;

    @TableField("vector_doc_id")
    private String vectorDocId;

    @TableField("metadata_json")
    private String metadataJson;

    @TableField("created_at")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Integer getChunkNo() {
        return chunkNo;
    }

    public void setChunkNo(Integer chunkNo) {
        this.chunkNo = chunkNo;
    }

    public String getChunkText() {
        return chunkText;
    }

    public void setChunkText(String chunkText) {
        this.chunkText = chunkText;
    }

    public Integer getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(Integer tokenCount) {
        this.tokenCount = tokenCount;
    }

    public String getVectorDocId() {
        return vectorDocId;
    }

    public void setVectorDocId(String vectorDocId) {
        this.vectorDocId = vectorDocId;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
