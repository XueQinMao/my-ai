package com.my.ai.cursor.ai.platform.application.pojo.dto;

import com.my.ai.cursor.ai.platform.application.agent.AgentRunStatus;
import com.my.ai.cursor.common.enums.AiScene;

import java.util.List;

public record AgentExecutionResultDto(String runId, AiScene scene, AgentRunStatus status, String finalAnswer,
                                      List<AgentToolTraceDto> toolTraces, String errorMessage) {

    public static AgentExecutionResultDto success(String runId, String content, List<AgentToolTraceDto> traces) {
        return new AgentExecutionResultDto(runId, AiScene.AGENT_CHAT, AgentRunStatus.COMPLETED, content, traces, null);
    }

    public static AgentExecutionResultDto failure(String runId, List<AgentToolTraceDto> traces, String errorMessage) {
        return new AgentExecutionResultDto(runId, AiScene.AGENT_CHAT, AgentRunStatus.FAILED, null, traces,
            errorMessage);
    }
}
