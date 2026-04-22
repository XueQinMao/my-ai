package com.my.ai.cursor.memory.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("agent_memory_vector_ref")
public class AgentMemoryVectorRef {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("memory_id")
    private Long memoryId;

    @TableField("vector_doc_id")
    private String vectorDocId;

    @TableField("embedding_model")
    private String embeddingModel;

    @TableField("created_at")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMemoryId() {
        return memoryId;
    }

    public void setMemoryId(Long memoryId) {
        this.memoryId = memoryId;
    }

    public String getVectorDocId() {
        return vectorDocId;
    }

    public void setVectorDocId(String vectorDocId) {
        this.vectorDocId = vectorDocId;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
