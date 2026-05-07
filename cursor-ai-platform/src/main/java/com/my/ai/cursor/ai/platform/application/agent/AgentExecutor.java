package com.my.ai.cursor.ai.platform.application.agent;

import com.my.ai.cursor.ai.platform.application.AiGatewayService;
import com.my.ai.cursor.ai.platform.application.context.ContextRunner;
import com.my.ai.cursor.ai.platform.application.pojo.AgentRunTracker;
import com.my.ai.cursor.ai.platform.application.context.AgentContext;
import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentRunResult;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
public class AgentExecutor {

    private final AiGatewayService aiGatewayService;
    private final AgentToolRegistry agentToolRegistry;
    private final AgentRunTracker agentRunTracker;
    private final ContextRunner contextRunner;

    public AgentExecutor(AiGatewayService aiGatewayService, AgentToolRegistry agentToolRegistry,
        AgentRunTracker agentRunTracker, ContextRunner contextRunner) {
        this.aiGatewayService = aiGatewayService;
        this.agentToolRegistry = agentToolRegistry;
        this.agentRunTracker = agentRunTracker;
        this.contextRunner = contextRunner;
    }

    public AgentRunResult execute(AgentContext context, Prompt prompt) {
        String runId = context.runId();
        return contextRunner.withAgentContext(context, () -> {
            String content = aiGatewayService.chat(context.scene(), prompt, agentToolRegistry.getTools());
            return AgentRunResult.success(runId, content, agentRunTracker.toolTraces());
        });
    }
}
