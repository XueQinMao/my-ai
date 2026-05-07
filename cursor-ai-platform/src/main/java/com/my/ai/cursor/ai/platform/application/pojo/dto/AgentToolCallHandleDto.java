package com.my.ai.cursor.ai.platform.application.pojo.dto;

import java.time.Instant;

public record AgentToolCallHandleDto(
    int stepNo, 
    String toolName, 
    String argumentsSummary, 
    long startedAtNanos,
    Instant startedAt,
    String taskDescription
) {
}
