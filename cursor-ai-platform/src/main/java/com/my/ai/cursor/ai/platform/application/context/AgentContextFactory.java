package com.my.ai.cursor.ai.platform.application.context;

/**
 * AgentContextFactory 负责把请求级上下文包装成 agent 运行上下文。
 */
public class AgentContextFactory {

    public static AgentContext create(RequestContext requestContext, String initialQuestion, Boolean enableKnowledge,
        Boolean enableLongTermMemory, Integer memoryWindow, int maxSteps, int maxToolCallsPerRun) {
        return AgentContext.of(requestContext, initialQuestion, enableKnowledge, enableLongTermMemory, memoryWindow,
            maxSteps, maxToolCallsPerRun);
    }
}
