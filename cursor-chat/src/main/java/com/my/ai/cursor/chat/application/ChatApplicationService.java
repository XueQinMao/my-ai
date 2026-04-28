package com.my.ai.cursor.chat.application;

import com.lmax.disruptor.dsl.Disruptor;
import com.my.ai.cursor.ai.platform.application.agent.AgentExecutor;
import com.my.ai.cursor.ai.platform.application.config.AgentRuntimeProperties;
import com.my.ai.cursor.ai.platform.application.context.AgentContextFactory;
import com.my.ai.cursor.ai.platform.application.context.RequestContextFactory;
import com.my.ai.cursor.ai.platform.application.agent.AgentRunStatus;
import com.my.ai.cursor.ai.platform.application.context.AgentContext;
import com.my.ai.cursor.ai.platform.application.context.RequestContext;
import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentRunResult;
import com.my.ai.cursor.chat.application.event.ChatCompleteEvent;
import com.my.ai.cursor.chat.application.pojo.req.ChatRequest;
import com.my.ai.cursor.chat.application.pojo.resp.AgentChatResponse;
import com.my.ai.cursor.common.enums.AiScene;
import com.my.ai.cursor.memory.application.ShortTermMemoryService;
import com.my.ai.cursor.memory.application.config.AppMemoryProperties;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.Map;

@Service
public class ChatApplicationService {

    private final AgentExecutor agentExecutor;

    private final AgentRuntimeProperties agentRuntimeProperties;

    private final AppMemoryProperties appMemoryProperties;

    private final ShortTermMemoryService shortTermMemoryService;

    private final Disruptor<ChatCompleteEvent> chatCompleteDisruptor;

    public ChatApplicationService(AgentExecutor agentExecutor, AgentRuntimeProperties agentRuntimeProperties,
        AppMemoryProperties appMemoryProperties, ShortTermMemoryService shortTermMemoryService,
        Disruptor<ChatCompleteEvent> chatCompleteDisruptor) {
        this.agentExecutor = agentExecutor;
        this.agentRuntimeProperties = agentRuntimeProperties;
        this.appMemoryProperties = appMemoryProperties;
        this.shortTermMemoryService = shortTermMemoryService;
        this.chatCompleteDisruptor = chatCompleteDisruptor;
    }

    public AgentChatResponse agentChat(ChatRequest request) {
        AgentContext context = createAgentContext(request, "http", "chat.agent");
        AgentRunResult result = agentExecutor.execute(context, generatePrompt(request));
        String assistant = resolveAgentMessage(result);
        // 先沿用现有聊天落库链路，保证 agent 输出也能进入会话历史和记忆系统。
        publishChatComplete(request, assistant, result, context.request());
        return AgentChatResponse.of(result.runId(), request.userId(), request.sessionId(), result.status(), assistant,
            result.toolTraces(), result.errorMessage());
    }

    public Flux<String> agentStreamChat(ChatRequest request) {
        return Flux.defer(() -> {
            AgentContext context = createAgentContext(request, "sse", "chat.agent.stream");
            AgentRunResult result = agentExecutor.execute(context, generatePrompt(request));
            if (result.status() == AgentRunStatus.FAILED) {
                return Flux.error(new IllegalStateException(result.errorMessage()));
            }
            String assistantMessage = resolveAgentMessage(result);
            publishChatComplete(request, assistantMessage, result, context.request());
            return Flux.just(assistantMessage);
        });
    }

    private Prompt generatePrompt(ChatRequest request) {
        String historyContext = defaultContext(
            appMemoryProperties.getShortTerm().isEnabled() ? shortTermMemoryService.generateShortMemoryContext(
                request.sessionId(), request.memoryWindow()) : null);
        // Agent prompt 只提供最小必要上下文，外部知识与长期记忆交给 tool calling 动态获取。
        PromptTemplate promptTemplate = new PromptTemplate("""
            你正在处理一个需要工具增强的用户请求。
            你可以在需要时调用知识库、长期记忆和最近历史工具。
            如果最近对话已经足够解释上下文，可以直接利用历史工具补全事实。
            不要编造资料；当工具结果不足时请明确说明信息不足。
            
            【最近对话窗口】
            {historyContext}
            
            【用户问题】
            {message}
            """);
        return promptTemplate.create(Map.of("historyContext", historyContext, "message", request.message()));
    }

    private String resolveAgentMessage(AgentRunResult result) {
        if (result.status() == AgentRunStatus.COMPLETED) {
            return result.finalAnswer();
        }
        // 对外统一转成可读错误文案，内部详细异常仍保留在 agent trace 和日志里。
        return StringUtils.hasText(result.errorMessage()) ? "Agent 执行失败：" + result.errorMessage()
            : "Agent 执行失败，请稍后重试。";
    }

    private void publishChatComplete(ChatRequest request, String assistantMessage, AgentRunResult result,
        RequestContext requestContext) {
        chatCompleteDisruptor.getRingBuffer().publishEvent((event, sequence) -> {
            event.setAssistant(assistantMessage);
            event.setRequest(request);
            event.setRequestContext(requestContext);
            event.setAgentRunResult(result);
        });
    }

    private AgentContext createAgentContext(ChatRequest request, String channel, String source) {
        RequestContext requestContext =
            RequestContextFactory.create(AiScene.AGENT_CHAT, request.userId(), request.sessionId(), channel, source);
        return AgentContextFactory.create(requestContext, request.message(), request.enableKnowledge(),
            request.enableLongTermMemory(), request.memoryWindow(), agentRuntimeProperties.getMaxSteps(),
            agentRuntimeProperties.getMaxToolCallsPerRun());
    }

    private String defaultContext(String context) {
        return StringUtils.hasText(context) ? context : "无";
    }
}
