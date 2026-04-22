package com.my.ai.cursor.knowledge.application.ingestion.pipeline;

import com.my.ai.cursor.knowledge.application.PipelineStep;
import com.my.ai.cursor.knowledge.application.pojo.comtext.IngestionContext;
import com.my.ai.cursor.knowledge.application.strategy.TextCleanerStrategy;
import org.springframework.stereotype.Component;

/**
 * ContentCleanProcessing
 *
 * @author 刘强
 * @version 2026/04/13 17:23
 **/
@Component
public class ContentCleanProcessing implements PipelineStep<IngestionContext, IngestionContext> {

    private final TextCleanerStrategy textCleanerStrategy;

    public ContentCleanProcessing(TextCleanerStrategy textCleanerStrategy) {
        this.textCleanerStrategy = textCleanerStrategy;
    }

    @Override
    public IngestionContext execute(IngestionContext input) {
        var cleanText = textCleanerStrategy.clean(input.getExtractedText(), input.getRequest());
        input.setCleanedText(cleanText);
        input.setExtractedText(null);
        return input;
    }

    @Override
    public int order() {
        return 2;
    }
}
