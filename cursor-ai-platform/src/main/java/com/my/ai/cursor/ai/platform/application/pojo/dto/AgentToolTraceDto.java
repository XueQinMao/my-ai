package com.my.ai.cursor.ai.platform.application.pojo.dto;

import java.time.Instant;

public record AgentToolTraceDto(
    int stepNo,
    String toolName,
    String taskDescription,
    String argumentsSummary,
    String resultSummary,
    String status,
    long durationMs,
    String errorMessage,
    Instant startedAt,
    Instant completedAt
) {
    public static AgentToolTraceDto of(int stepNo, String toolName, String taskDescription, 
                                       String argumentsSummary, String resultSummary,
                                       String status, long durationMs, String errorMessage,
                                       Instant startedAt, Instant completedAt) {
        return new AgentToolTraceDto(stepNo, toolName, taskDescription, argumentsSummary, 
                                     resultSummary, status, durationMs, errorMessage,
                                     startedAt, completedAt);
    }
}
