package com.my.ai.cursor.ai.platform.application.advisors;

import com.lmax.disruptor.dsl.Disruptor;
import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentExecutionRecorderDto;
import com.my.ai.cursor.ai.platform.application.pojo.context.AgentExecutionContext;
import com.my.ai.cursor.ai.platform.application.event.LlmTokenCostEvent;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * LlmLogAdvisor
 *
 * @author 刘强
 * @version 2026/04/16 19:32
 **/
@Component
public class LlmLogAdvisor implements CallAdvisor, StreamAdvisor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AgentExecutionRecorderDto agentExecutionRecorderDto;

    @Resource(name = "llmTokenCostEventDisruptor")
    private Disruptor<LlmTokenCostEvent> disruptor;

    public LlmLogAdvisor(AgentExecutionRecorderDto agentExecutionRecorderDto) {
        this.agentExecutionRecorderDto = agentExecutionRecorderDto;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        logRequest(chatClientRequest);

        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);

        logResponse(chatClientResponse);
        sendTokenCostEvent(1L, chatClientResponse);
        return chatClientResponse;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
        StreamAdvisorChain streamAdvisorChain) {
        logRequest(chatClientRequest);

        Flux<ChatClientResponse> chatClientResponses = streamAdvisorChain.nextStream(chatClientRequest);

        return new ChatClientMessageAggregator().aggregateChatClientResponse(chatClientResponses, resp ->{
            logResponse(resp);
            sendTokenCostEvent(1L, resp);
        });
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private void logRequest(ChatClientRequest request) {
        AgentExecutionContext context = agentExecutionRecorderDto.currentContext();
        if (context == null) {
            logger.info("llm request: {}", request);
            return;
        }
        // 进入 agent 模式后，把 runId/userId/sessionId 串进日志，便于排查某次工具链路的完整上下文。
        logger.info("llm request. runId={}, userId={}, sessionId={}, request={}", context.runId(), context.userId(),
            context.sessionId(), request);
    }

    private void logResponse(ChatClientResponse chatClientResponse) {
        AgentExecutionContext context = agentExecutionRecorderDto.currentContext();
        if (context == null) {
            logger.info("llm response: {}", chatClientResponse);
            return;
        }
        logger.info("llm response. runId={}, userId={}, sessionId={}, response={}", context.runId(), context.userId(),
            context.sessionId(), chatClientResponse);
    }

    private void sendTokenCostEvent(Long chatId, ChatClientResponse chatClientResponse) {
        AgentExecutionContext context = agentExecutionRecorderDto.currentContext();
        var model = chatClientResponse.chatResponse().getMetadata().getModel();
        var totalTokens = chatClientResponse.chatResponse().getMetadata().getUsage().getTotalTokens();
        var promptTokens = chatClientResponse.chatResponse().getMetadata().getUsage().getPromptTokens();
        var completionTokens = chatClientResponse.chatResponse().getMetadata().getUsage().getCompletionTokens();
        disruptor.getRingBuffer().publishEvent((event, sequence) -> {
            // userId 目前事件里仍是 Long，因此这里做一次兼容转换，避免字符串用户标识直接打断埋点。
            event.setUserId(parseUserId(context == null ? null : context.userId()));
            event.setChatId(chatId);
            event.setModelName(model);
            event.setTokenCount(totalTokens);
            event.setPromptTokens(promptTokens);
            event.setCompletionTokens(completionTokens);
        });
    }

    private Long parseUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
