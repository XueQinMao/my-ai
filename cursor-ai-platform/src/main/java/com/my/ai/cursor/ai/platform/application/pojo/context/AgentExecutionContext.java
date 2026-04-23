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

    public static AgentExecutionContext of(String runId, String userId, String sessionId, AiScene scene) {
        return new AgentExecutionContext(runId, userId, sessionId, scene, 100, 10);
    }
    public static AgentExecutionContext of(String runId, String userId, String sessionId, AiScene scene, int maxSteps,
        int maxToolCallsPerRun) {
        return new AgentExecutionContext(runId, userId, sessionId, scene, maxSteps, maxToolCallsPerRun);
    }
}
