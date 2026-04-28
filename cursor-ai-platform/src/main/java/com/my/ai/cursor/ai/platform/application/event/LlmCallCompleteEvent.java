package com.my.ai.cursor.ai.platform.application.event;

import com.lmax.disruptor.EventFactory;
import com.my.ai.cursor.ai.platform.application.context.LlmCallContext;
import com.my.ai.cursor.ai.platform.application.context.RequestContext;
import com.my.ai.cursor.common.enums.AiScene;
import lombok.Data;

import java.time.Instant;

/**
 * LlmCallCompleteEvent
 *
 * @author 刘强
 * @version 2026/04/17 15:19
 **/
@Data
public class LlmCallCompleteEvent {

    private Long userId;

    private RequestContext requestContext;

    private LlmCallContext llmCallContext;

    /**
     * 聊天ID
     */
    private Long chatId;

    private String modelName;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer tokenCount;

    private Instant startedAt;

    private Boolean isStreaming;

    private AiScene scene;

    public static final EventFactory<LlmCallCompleteEvent> FACTORY = LlmCallCompleteEvent::new;
}
