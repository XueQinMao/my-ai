package com.my.ai.cursor.ai.platform.application.event.handler;

import com.alibaba.fastjson.JSON;
import com.lmax.disruptor.EventHandler;
import com.my.ai.cursor.ai.platform.application.event.LlmCallCompleteEvent;
import com.my.ai.cursor.ai.platform.application.observability.AiCostBreakdown;
import com.my.ai.cursor.ai.platform.application.observability.AiCostCalculator;
import com.my.ai.cursor.ai.platform.application.observability.AiMetricsRecorder;
import com.my.ai.cursor.ai.platform.application.context.LlmCallContext;
import com.my.ai.cursor.ai.platform.application.context.RequestContext;
import com.my.ai.cursor.common.enums.AiScene;
import com.my.ai.cursor.ai.platform.domain.KbTokenCostRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * LlmCallCompleteEventHandler
 *
 * @author 刘强
 * @version 2026/04/17 15:33
 **/
@Component
public class LlmCallCompleteEventHandler implements EventHandler<LlmCallCompleteEvent> {

    private final KbTokenCostRecordRepository kbTokenCostRecordRepository;

    private final AiCostCalculator aiCostCalculator;

    private static final Logger log = LoggerFactory.getLogger(LlmCallCompleteEventHandler.class);

    public LlmCallCompleteEventHandler(KbTokenCostRecordRepository kbTokenCostRecordRepository,
        AiCostCalculator aiCostCalculator) {
        this.kbTokenCostRecordRepository = kbTokenCostRecordRepository;
        this.aiCostCalculator = aiCostCalculator;
    }

    @Override
    public void onEvent(LlmCallCompleteEvent event, long sequence, boolean endOfBatch) {
        log.info("LlmCallCompleteEvent:{} 位置:{} 是否结尾:{}", JSON.toJSONString(event), sequence, endOfBatch);

        RequestContext requestContext = event.getRequestContext();
        LlmCallContext llmCallContext = event.getLlmCallContext();
        AiScene scene = requestContext == null || requestContext.scene() == null ? defaultScene(event.getScene())
            : requestContext.scene();
        String modelName = resolveModelName(event, llmCallContext);

        //记录日志
        AiCostBreakdown costBreakdown =
            aiCostCalculator.calculate(modelName, event.getPromptTokens(), event.getCompletionTokens());

        AiMetricsRecorder.recordLlmCall(scene.name(), modelName, "SUCCESS", Boolean.TRUE.equals(event.getIsStreaming()),
            event.getStartedAt() == null ? 0L : ChronoUnit.MILLIS.between(event.getStartedAt(), Instant.now()),
            event.getPromptTokens(), event.getCompletionTokens(), event.getTokenCount(),
            costBreakdown.totalCost().doubleValue());

        kbTokenCostRecordRepository.insert(event.getUserId(), event.getChatId(), modelName,
            event.getTokenCount(), event.getPromptTokens(), event.getCompletionTokens());
    }

    private String resolveModelName(LlmCallCompleteEvent event, LlmCallContext llmCallContext) {
        if (StringUtils.hasText(event.getModelName())) {
            return event.getModelName();
        }
        if (llmCallContext != null && StringUtils.hasText(llmCallContext.modelName())) {
            return llmCallContext.modelName();
        }
        return "unknown";
    }

    private AiScene defaultScene(AiScene scene) {
        return scene == null ? AiScene.NORMAL_CHAT : scene;
    }
}
