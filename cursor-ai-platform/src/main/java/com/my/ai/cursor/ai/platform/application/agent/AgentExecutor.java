package com.my.ai.cursor.ai.platform.application.agent;

import com.my.ai.cursor.ai.platform.application.AiGatewayService;
import com.my.ai.cursor.ai.platform.application.ChatClientRouter;
import com.my.ai.cursor.ai.platform.application.config.AgentRuntimeProperties;
import com.my.ai.cursor.ai.platform.application.pojo.context.AgentExecutionContext;
import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentExecutionRecorderDto;
import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentExecutionResultDto;
import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentToolTraceDto;
import com.my.ai.cursor.common.enums.AiScene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@Service
public class AgentExecutor {

    private static final Logger log = LoggerFactory.getLogger(AgentExecutor.class);

    private final AiGatewayService aiGatewayService;
    private final AgentToolRegistry agentToolRegistry;
    private final AgentExecutionRecorderDto agentExecutionRecorderDto;
    private final AgentRuntimeProperties properties;

    public AgentExecutor(AiGatewayService aiGatewayService, AgentToolRegistry agentToolRegistry,
        AgentExecutionRecorderDto agentExecutionRecorderDto, AgentRuntimeProperties agentRuntimeProperties) {
        this.aiGatewayService = aiGatewayService;
        this.agentToolRegistry = agentToolRegistry;
        this.agentExecutionRecorderDto = agentExecutionRecorderDto;
        this.properties = agentRuntimeProperties;
    }

    public AgentExecutionResultDto execute(String userId, String sessionId, Prompt prompt) {
        String runId = UUID.randomUUID().toString();
        // 一次 agent 执行对应一个 runId，工具调用轨迹与日志都会挂在这个上下文下。
        AgentExecutionContext context =
            AgentExecutionContext.of(runId, userId, sessionId, AiScene.AGENT_CHAT, properties.getMaxSteps(),
                properties.getMaxToolCallsPerRun());
        agentExecutionRecorderDto.startRun(context);
        try {
            // 这里先使用 Spring AI 内置的 tool calling 循环，让模型自主决定是否调用工具并汇总最终答案。
            String content = aiGatewayService.chat(AiScene.AGENT_CHAT, prompt, agentToolRegistry.resolveTools());
            List<AgentToolTraceDto> traces = agentExecutionRecorderDto.snapshot();
            log.info("agent run completed. runId={}, sessionId={}, userId={}, toolCalls={}", runId, sessionId, userId,
                traces.size());
            return new AgentExecutionResultDto(runId, AiScene.AGENT_CHAT, AgentRunStatus.COMPLETED, content,
                traces.size(), traces, null);
        } catch (Exception e) {
            List<AgentToolTraceDto> traces = agentExecutionRecorderDto.snapshot();
            log.error("agent run failed. runId={}, sessionId={}, userId={}", runId, sessionId, userId, e);
            return new AgentExecutionResultDto(runId, AiScene.AGENT_CHAT, AgentRunStatus.FAILED, null, traces.size(),
                traces, e.getMessage());
        } finally {
            agentExecutionRecorderDto.finishRun();
        }
    }

    public Flux<String> stream(String userId, String sessionId, Prompt prompt) {
        return Flux.defer(() -> {
            // 当前的 stream 是“执行完成后统一输出”，后续如果要做真正逐步流式，可在这里拆成 step 级事件流。
            AgentExecutionResultDto result = execute(userId, sessionId, prompt);
            if (result.status() == AgentRunStatus.FAILED) {
                return Flux.error(new IllegalStateException(result.errorMessage()));
            }
            return Flux.just(result.finalAnswer());
        });
    }
}
