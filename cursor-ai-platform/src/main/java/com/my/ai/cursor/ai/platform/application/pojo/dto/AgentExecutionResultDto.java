package com.my.ai.cursor.ai.platform.application.pojo.dto;

import com.my.ai.cursor.ai.platform.application.agent.AgentRunStatus;
import com.my.ai.cursor.common.enums.AiScene;

import java.util.List;

public record AgentExecutionResultDto(
    String runId,
    AiScene scene,
    AgentRunStatus status,
    String finalAnswer,
    int toolCallCount,
    List<AgentToolTraceDto> toolTraces,
    String errorMessage
) {
}
