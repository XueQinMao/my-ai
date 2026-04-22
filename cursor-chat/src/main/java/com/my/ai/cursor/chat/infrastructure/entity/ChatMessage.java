package com.my.ai.cursor.chat.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("chat_message")
public class ChatMessage {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("session_id")
    private String sessionId;

    @TableField("user_id")
    private String userId;

    @TableField("role")
    private String role;

    @TableField("content")
    private String content;

    @TableField("tokens_input")
    private Integer tokensInput;

    @TableField("tokens_output")
    private Integer tokensOutput;

    @TableField("trace_id")
    private String traceId;

    @TableField("created_at")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getTokensInput() {
        return tokensInput;
    }

    public void setTokensInput(Integer tokensInput) {
        this.tokensInput = tokensInput;
    }

    public Integer getTokensOutput() {
        return tokensOutput;
    }

    public void setTokensOutput(Integer tokensOutput) {
        this.tokensOutput = tokensOutput;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
