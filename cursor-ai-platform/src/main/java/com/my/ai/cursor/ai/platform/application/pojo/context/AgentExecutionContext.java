package com.my.ai.cursor.ai.platform.application.pojo.context;

import com.my.ai.cursor.common.enums.AiScene;

public record AgentExecutionContext(
    String runId,
    String userId,
    String sessionId,
    AiScene scene,
    int maxSteps,
    int maxToolCallsPerRun
) {

    public static AgentExecutionContext of(String runId, String userId, String sessionId) {
        return new AgentExecutionContext(runId, userId, sessionId, AiScene.AGENT_CHAT, 100, 10);
    }
    public static AgentExecutionContext of(String runId, String userId, String sessionId, int maxSteps,
        int maxToolCallsPerRun) {
        return new AgentExecutionContext(runId, userId, sessionId, AiScene.AGENT_CHAT, maxSteps, maxToolCallsPerRun);
    }
}
