package com.my.ai.cursor.ai.platform.application.advisors;

import com.lmax.disruptor.dsl.Disruptor;
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

    @Resource(name = "llmTokenCostEventDisruptor")
    private Disruptor<LlmTokenCostEvent> disruptor;

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
        logger.info("llm request: {}", request);
    }

    private void logResponse(ChatClientResponse chatClientResponse) {
        logger.info("llm response: {}", chatClientResponse);
    }

    private void sendTokenCostEvent(Long chatId, ChatClientResponse chatClientResponse) {
        var model = chatClientResponse.chatResponse().getMetadata().getModel();
        var totalTokens = chatClientResponse.chatResponse().getMetadata().getUsage().getTotalTokens();
        var promptTokens = chatClientResponse.chatResponse().getMetadata().getUsage().getPromptTokens();
        var completionTokens = chatClientResponse.chatResponse().getMetadata().getUsage().getCompletionTokens();
        disruptor.getRingBuffer().publishEvent((event, sequence) -> {
            event.setUserId(1L);
            event.setChatId(chatId);
            event.setModelName(model);
            event.setTokenCount(totalTokens);
            event.setPromptTokens(promptTokens);
            event.setCompletionTokens(completionTokens);
        });
    }
}
