package com.my.ai.cursor.ai.platform.application.agent;

import com.my.ai.cursor.ai.platform.application.AiGatewayService;
import com.my.ai.cursor.ai.platform.application.context.ContextRunner;
import com.my.ai.cursor.ai.platform.application.observability.AiMetricsRecorder;
import com.my.ai.cursor.ai.platform.application.pojo.AgentRunTracker;
import com.my.ai.cursor.ai.platform.application.context.AgentContext;
import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentRunResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AgentExecutor {

    private static final Logger log = LoggerFactory.getLogger(AgentExecutor.class);

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
        Instant startedAt = context.request().startedAt();
        String runId = context.runId();
        return contextRunner.withAgentContext(context, () -> {
            // 这里先使用 Spring AI 内置的 tool calling 循环，让模型自主决定是否调用工具并汇总最终答案。
            try {
                String content = aiGatewayService.chat(context.scene(), prompt, agentToolRegistry.getTools());
                AiMetricsRecorder.recordAgentRun(context.scene().name(), AgentRunStatus.COMPLETED.name(), startedAt,
                    agentRunTracker.currentToolCallCount());
                return AgentRunResult.success(runId, content, agentRunTracker.toolTraces());
            } catch (Exception e) {
                AiMetricsRecorder.recordAgentRun(context.scene().name(), AgentRunStatus.FAILED.name(), startedAt,
                    agentRunTracker.currentToolCallCount());
                log.error("Agent run failed. runId={}, sessionId={}", runId, context.sessionId(), e);
                return AgentRunResult.failure(runId, agentRunTracker.toolTraces(), e.getMessage());
            }
        });
    }
}
