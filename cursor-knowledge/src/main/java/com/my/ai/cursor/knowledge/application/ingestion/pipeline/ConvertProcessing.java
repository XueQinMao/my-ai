package com.my.ai.cursor.knowledge.application.ingestion.pipeline;

import com.my.ai.cursor.knowledge.application.cleaning.ChunkBuilder;
import com.my.ai.cursor.knowledge.application.PipelineStep;
import com.my.ai.cursor.knowledge.application.pojo.comtext.IngestionContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * ConvertProcessing
 *
 * @author 刘强
 * @version 2026/04/14 17:37
 **/
@Component
public class ConvertProcessing implements PipelineStep<IngestionContext, IngestionContext> {

    private final ChunkBuilder chunkBuilder;

    public ConvertProcessing(ChunkBuilder chunkBuilder) {
        this.chunkBuilder = chunkBuilder;
    }

    @Override
    public IngestionContext execute(IngestionContext input) {
        input.setTitle(resolveTitle(input));
        input.setChunks(chunkBuilder.build(input.getCleanedText()));
        if (input.getChunks().isEmpty()) {
            throw new IllegalArgumentException("No valid content found after PDF extraction and cleaning");
        }
        return input;
    }

    @Override
    public int order() {
        return 3;
    }

    private String resolveTitle(IngestionContext input) {
        if (StringUtils.hasText(input.getRequest().title())) {
            return input.getRequest().title().trim();
        }
        String cleaned = input.getCleanedText() == null ? "" : input.getCleanedText().replaceAll("\\s+", " ").trim();
        if (StringUtils.hasText(cleaned)) {
            return cleaned.length() > 48 ? cleaned.substring(0, 48) : cleaned;
        }
        return input.getRequest().sourceUrl();
    }
}
