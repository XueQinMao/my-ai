package com.my.ai.cursor.ai.platform.application.advisors;

import com.lmax.disruptor.dsl.Disruptor;
import com.my.ai.cursor.ai.platform.application.context.ContextRunner;
import com.my.ai.cursor.ai.platform.application.context.LlmCallContextFactory;
import com.my.ai.cursor.ai.platform.application.event.LlmCallCompleteEvent;
import com.my.ai.cursor.ai.platform.application.pojo.AgentRunTracker;
import com.my.ai.cursor.ai.platform.application.pojo.RequestContextTracker;
import com.my.ai.cursor.ai.platform.application.context.LlmCallContext;
import com.my.ai.cursor.ai.platform.application.context.RequestContext;
import com.my.ai.cursor.common.enums.AiScene;
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
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.time.Instant;

/**
 * LlmLogAdvisor
 *
 * @author 刘强
 * @version 2026/04/16 19:32
 **/
@Component
public class LlmLogAdvisor implements CallAdvisor, StreamAdvisor {

    private static final String UNKNOWN_MODEL = "unknown";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AgentRunTracker agentRunTracker;

    private final RequestContextTracker requestContextTracker;

    private final LlmCallContextFactory llmCallContextFactory;

    private final ContextRunner contextRunner;

    @Resource(name = "llmTokenCostEventDisruptor")
    private Disruptor<LlmCallCompleteEvent> disruptor;

    public LlmLogAdvisor(AgentRunTracker agentRunTracker, RequestContextTracker requestContextTracker,
        LlmCallContextFactory llmCallContextFactory, ContextRunner contextRunner) {
        this.agentRunTracker = agentRunTracker;
        this.requestContextTracker = requestContextTracker;
        this.llmCallContextFactory = llmCallContextFactory;
        this.contextRunner = contextRunner;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        Instant startedAt = Instant.now();
        LlmCallContext llmCallContext = buildLlmCallContext(chatClientRequest, false, startedAt);
        return contextRunner.withLlmCallContext(llmCallContext, () -> {
            logRequest(chatClientRequest, llmCallContext);
            try {
                ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
                observeResponse(chatClientResponse, llmCallContext, false);
                return chatClientResponse;
            } catch (Exception e) {
                logError(llmCallContext, e);
                throw e;
            }
        });
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
        StreamAdvisorChain streamAdvisorChain) {
        Instant startedAt = Instant.now();
        LlmCallContext llmCallContext = buildLlmCallContext(chatClientRequest, true, startedAt);
        logRequest(chatClientRequest, llmCallContext);
        Flux<ChatClientResponse> chatClientResponses = streamAdvisorChain.nextStream(chatClientRequest);
        Flux<ChatClientResponse> observedResponses =
            chatClientResponses.doOnError(error -> logError(llmCallContext, error));
        return new ChatClientMessageAggregator().aggregateChatClientResponse(observedResponses,
            resp -> observeResponse(resp, llmCallContext, true));
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private void logRequest(ChatClientRequest request, LlmCallContext llmCallContext) {
        if (llmCallContext == null || llmCallContext.request() == null) {
            logger.info("llm request: {}", request);
            return;
        }
        RequestContext requestContext = llmCallContext.request();
        logger.info(
            "llm request. callId={}, requestId={}, runId={}, scene={}, userId={}, sessionId={}, channel={}, request={}",
            llmCallContext.callId(), requestContext.requestId(), requestContext.runId(), requestContext.scene(),
            requestContext.userId(), requestContext.sessionId(), requestContext.channel(), request);
    }

    private void logResponse(ChatClientResponse chatClientResponse, LlmCallContext llmCallContext) {
        if (llmCallContext == null || llmCallContext.request() == null) {
            logger.info("llm response: {}", chatClientResponse);
            return;
        }
        RequestContext requestContext = llmCallContext.request();
        logger.info("llm response. callId={}, requestId={}, runId={}, scene={}, userId={}, sessionId={}, response={}",
            llmCallContext.callId(), requestContext.requestId(), requestContext.runId(), requestContext.scene(),
            requestContext.userId(), requestContext.sessionId(), chatClientResponse);
    }

    private void observeResponse(ChatClientResponse chatClientResponse, LlmCallContext llmCallContext,
        boolean streaming) {
        logResponse(chatClientResponse, llmCallContext);
        var metadata = chatClientResponse.chatResponse().getMetadata();
        var model = !StringUtils.hasText(metadata.getModel()) ? UNKNOWN_MODEL : metadata.getModel();
        var usage = metadata.getUsage();
        var totalTokens = usage.getTotalTokens();
        var promptTokens = usage.getPromptTokens();
        var completionTokens = usage.getCompletionTokens();
        LlmCallContext completedContext = llmCallContext == null ? null : llmCallContext.withModelName(model);
        RequestContext requestContext = completedContext == null ? null : completedContext.request();
        disruptor.getRingBuffer().publishEvent((event, sequence) -> {
            // ThreadLocal 无法跨过 Disruptor，因此这里显式把 request/llm 快照塞进事件对象。
            event.setRequestContext(requestContext);
            event.setLlmCallContext(completedContext);
            event.setUserId(parseUserId(requestContext));
            event.setChatId(0L);
            event.setModelName(model);
            event.setTokenCount(totalTokens);
            event.setPromptTokens(promptTokens);
            event.setCompletionTokens(completionTokens);
            event.setScene(requestContext == null || requestContext.scene() == null ? AiScene.NORMAL_CHAT
                : requestContext.scene());
            event.setStartedAt(completedContext == null ? Instant.now() : completedContext.startedAt());
            event.setIsStreaming(streaming);
        });
    }

    private LlmCallContext buildLlmCallContext(ChatClientRequest request, boolean streaming, Instant startedAt) {
        RequestContext requestContext = resolveRequestContext();
        return llmCallContextFactory.create(requestContext, request, streaming, startedAt, UNKNOWN_MODEL,
            resolveBizAction(requestContext));
    }

    private RequestContext resolveRequestContext() {
        RequestContext requestContext = agentRunTracker.currentRequestContext();
        return requestContext != null ? requestContext : requestContextTracker.currentContext();
    }

    private String resolveBizAction(RequestContext requestContext) {
        if (requestContext == null || requestContext.scene() == null) {
            return "llm_call";
        }
        return switch (requestContext.scene()) {
            case AGENT_CHAT -> "agent_answer";
            case EVALUATION_CHAT -> "eval_judge";
            case MEMORY_EXTRACTION -> "memory_extract";
            case RAG_CLEANING -> "rag_clean";
            default -> "llm_call";
        };
    }

    private void logError(LlmCallContext llmCallContext, Throwable error) {
        if (llmCallContext == null || llmCallContext.request() == null) {
            logger.error("llm response error", error);
            return;
        }
        RequestContext requestContext = llmCallContext.request();
        logger.error("llm response error. callId={}, requestId={}, runId={}, scene={}", llmCallContext.callId(),
            requestContext.requestId(), requestContext.runId(), requestContext.scene(), error);
    }

    private long parseUserId(RequestContext requestContext) {
        if (requestContext == null || !StringUtils.hasText(requestContext.userId())) {
            return 0L;
        }
        try {
            return Long.parseLong(requestContext.userId());
        } catch (NumberFormatException e) {
            logger.warn("Skip numeric userId conversion. userId={}", requestContext.userId());
            return 0L;
        }
    }
}
