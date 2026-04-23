package com.my.ai.cursor.chat.application;

import com.lmax.disruptor.dsl.Disruptor;
import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentExecutionResultDto;
import com.my.ai.cursor.ai.platform.application.agent.AgentExecutor;
import com.my.ai.cursor.ai.platform.application.agent.AgentRunStatus;
import com.my.ai.cursor.chat.application.event.ChatCompleteEvent;
import com.my.ai.cursor.chat.application.pojo.req.ChatRequest;
import com.my.ai.cursor.chat.application.pojo.resp.AgentChatResponse;
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
    private final AppMemoryProperties appMemoryProperties;

    private final ShortTermMemoryService shortTermMemoryService;

    private final Disruptor<ChatCompleteEvent> chatCompleteDisruptor;

    public ChatApplicationService(AgentExecutor agentExecutor, AppMemoryProperties appMemoryProperties,
        ShortTermMemoryService shortTermMemoryService, Disruptor<ChatCompleteEvent> chatCompleteDisruptor) {
        this.agentExecutor = agentExecutor;
        this.appMemoryProperties = appMemoryProperties;
        this.shortTermMemoryService = shortTermMemoryService;
        this.chatCompleteDisruptor = chatCompleteDisruptor;
    }

    public AgentChatResponse agentChat(ChatRequest request) {
        // Agent 模式不再提前把知识和长期记忆硬塞进 prompt，而是让模型按需调用工具。
        AgentExecutionResultDto result =
            agentExecutor.execute(request.userId(), request.sessionId(), generatePrompt(request));
        String assistantMessage = resolveAgentMessage(result);
        // 先沿用现有聊天落库链路，保证 agent 输出也能进入会话历史和记忆系统。
        publishChatComplete(request, assistantMessage);
        return new AgentChatResponse(result.runId(), request.userId(), request.sessionId(),
            result.scene().name(), result.status(), assistantMessage, result.toolCallCount(), result.toolTraces(),
            result.errorMessage());
    }

    public Flux<String> agentStreamChat(ChatRequest request) {
        ;
        Prompt prompt = generatePrompt(request);
        StringBuilder assistantMessage = new StringBuilder();
        return agentExecutor.stream(request.userId(), request.sessionId(), prompt)
            .doOnNext(assistantMessage::append)
            .doOnComplete(() -> publishChatComplete(request, assistantMessage.toString()));
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

    private String resolveAgentMessage(AgentExecutionResultDto result) {
        if (result.status() == AgentRunStatus.COMPLETED) {
            return result.finalAnswer();
        }
        // 对外统一转成可读错误文案，内部详细异常仍保留在 agent trace 和日志里。
        return StringUtils.hasText(result.errorMessage()) ? "Agent 执行失败：" + result.errorMessage()
            : "Agent 执行失败，请稍后重试。";
    }

    private void publishChatComplete(ChatRequest request, String assistantMessage) {
        chatCompleteDisruptor.getRingBuffer().publishEvent((event, sequence) -> {
            event.setAssistantMessage(assistantMessage);
            event.setRequest(request);
        });
    }

    private String defaultContext(String context) {
        return StringUtils.hasText(context) ? context : "无";
    }
}
