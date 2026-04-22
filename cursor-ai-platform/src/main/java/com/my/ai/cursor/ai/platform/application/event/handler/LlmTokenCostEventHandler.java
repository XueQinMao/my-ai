package com.my.ai.cursor.ai.platform.application.event.handler;

import com.alibaba.fastjson.JSON;
import com.lmax.disruptor.EventHandler;
import com.my.ai.cursor.ai.platform.domain.KbTokenCostRecordRepository;
import com.my.ai.cursor.ai.platform.application.event.LlmTokenCostEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * LlmTokenCostEventHandler
 *
 * @author 刘强
 * @version 2026/04/17 15:33
 **/
@Component
public class LlmTokenCostEventHandler implements EventHandler<LlmTokenCostEvent> {

    private final KbTokenCostRecordRepository kbTokenCostRecordRepository;

    private static final Logger log = LoggerFactory.getLogger(LlmTokenCostEventHandler.class);

    public LlmTokenCostEventHandler(KbTokenCostRecordRepository kbTokenCostRecordRepository) {
        this.kbTokenCostRecordRepository = kbTokenCostRecordRepository;
    }

    @Override
    public void onEvent(LlmTokenCostEvent llmTokenCostEvent, long sequence, boolean endOfBatch) throws Exception {
        log.info("LlmTokenCostEvent:{} 位置:{} 是否结尾:{}", JSON.toJSONString(llmTokenCostEvent), sequence,
            endOfBatch);

        kbTokenCostRecordRepository.insert(llmTokenCostEvent.getUserId(), llmTokenCostEvent.getChatId(),
            llmTokenCostEvent.getModelName(), llmTokenCostEvent.getTokenCount(), llmTokenCostEvent.getPromptTokens(),
            llmTokenCostEvent.getCompletionTokens());
    }
}
