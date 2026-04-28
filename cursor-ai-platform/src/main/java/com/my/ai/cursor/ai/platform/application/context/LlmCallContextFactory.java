package com.my.ai.cursor.ai.platform.application.context;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * LlmCallContextFactory 负责创建单次模型调用快照。
 */
@Component
public class LlmCallContextFactory {

    public LlmCallContext create(RequestContext requestContext, ChatClientRequest request, boolean streaming,
        Instant startedAt, String modelName, String bizAction) {
        return LlmCallContext.of(requestContext, modelName, bizAction, streaming,
            request == null ? "unknown" : request.getClass().getSimpleName(), String.valueOf(request), startedAt);
    }
}
