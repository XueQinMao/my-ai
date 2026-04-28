package com.my.ai.cursor.ai.platform.application.pojo.dto;

import com.my.ai.cursor.ai.platform.application.agent.AgentRunStatus;
import com.my.ai.cursor.common.enums.AiScene;

import java.util.List;

public record AgentRunResult(String runId, AiScene scene, AgentRunStatus status, String finalAnswer,
                             List<AgentToolTraceDto> toolTraces, String errorMessage) {

    public static AgentRunResult success(String runId, String content, List<AgentToolTraceDto> traces) {
        return new AgentRunResult(runId, AiScene.AGENT_CHAT, AgentRunStatus.COMPLETED, content, traces, null);
    }

    public static AgentRunResult failure(String runId, List<AgentToolTraceDto> traces, String errorMessage) {
        return new AgentRunResult(runId, AiScene.AGENT_CHAT, AgentRunStatus.FAILED, null, traces,
            errorMessage);
    }
}
