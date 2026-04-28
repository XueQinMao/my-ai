package com.my.ai.cursor.ai.platform.application.context;

import com.my.ai.cursor.common.enums.AiScene;

public record AgentContext(RequestContext request, int maxSteps, int maxToolCallsPerRun, String initialQuestion,
                           int memoryWindow, boolean enableKnowledge, boolean enableLongTermMemory) {

    public static AgentContext of(RequestContext request, String initialQuestion, Boolean enableKnowledge,
        Boolean enableLongTermMemory, Integer memoryWindow, int maxSteps, int maxToolCallsPerRun) {
        return new AgentContext(request, maxSteps, maxToolCallsPerRun, initialQuestion,
            memoryWindow == null ? 0 : Math.max(0, memoryWindow), Boolean.TRUE.equals(enableKnowledge),
            Boolean.TRUE.equals(enableLongTermMemory));
    }

    public String runId() {
        return request.runId();
    }

    public String userId() {
        return request.userId();
    }

    public String sessionId() {
        return request.sessionId();
    }

    public AiScene scene() {
        return request.scene();
    }
}
