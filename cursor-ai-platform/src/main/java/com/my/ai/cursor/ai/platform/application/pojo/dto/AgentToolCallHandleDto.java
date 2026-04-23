package com.my.ai.cursor.ai.platform.application.pojo.dto;

public record AgentToolCallHandleDto(
    int stepNo,
    String toolName,
    String argumentsSummary,
    long startedAtNanos
) {
}
