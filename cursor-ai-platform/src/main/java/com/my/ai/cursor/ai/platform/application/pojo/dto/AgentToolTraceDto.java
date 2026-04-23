package com.my.ai.cursor.ai.platform.application.pojo.dto;

public record AgentToolTraceDto(
    int stepNo,
    String toolName,
    String argumentsSummary,
    String resultSummary,
    String status,
    long durationMs,
    String errorMessage
) {
}
