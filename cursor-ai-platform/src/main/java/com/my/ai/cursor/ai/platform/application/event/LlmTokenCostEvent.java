package com.my.ai.cursor.ai.platform.application.event;

import com.lmax.disruptor.EventFactory;
import lombok.Data;

/**
 * LlmTokenCostEvent
 *
 * @author 刘强
 * @version 2026/04/17 15:19
 **/
@Data
public class LlmTokenCostEvent {

    private Long userId;
    /**
     * 聊天ID
     */
    private Long chatId;

    private String modelName;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer tokenCount;

    public static final EventFactory<LlmTokenCostEvent> FACTORY = LlmTokenCostEvent::new;
}
