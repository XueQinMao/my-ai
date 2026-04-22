package com.my.ai.cursor.knowledge.application.ingestion.pipeline;

import com.my.ai.cursor.common.utils.PdfUtils;
import com.my.ai.cursor.knowledge.application.PipelineStep;
import com.my.ai.cursor.knowledge.application.pojo.comtext.IngestionContext;
import com.my.ai.cursor.knowledge.application.strategy.DocumentFetcherStrategy;
import com.my.ai.cursor.knowledge.application.strategy.TextExtractorStrategy;
import org.springframework.stereotype.Component;

/**
 * ContentExtractionProcessing
 *
 * @author 刘强
 * @version 2026/04/13 17:03
 **/
@Component
public class ContentExtractionProcessing implements PipelineStep<IngestionContext, IngestionContext> {

    private final DocumentFetcherStrategy documentFetcherStrategy;
    private final TextExtractorStrategy textExtractorStrategy;

    public ContentExtractionProcessing(DocumentFetcherStrategy documentFetcherStrategy,
                                       TextExtractorStrategy textExtractorStrategy) {
        this.documentFetcherStrategy = documentFetcherStrategy;
        this.textExtractorStrategy = textExtractorStrategy;
    }

    @Override
    public IngestionContext execute(IngestionContext input) {
        byte[] pdfBytes = documentFetcherStrategy.fetch(input.getRequest().sourceUrl());
        PdfUtils.PdfParseResult parseResult = textExtractorStrategy.extract(pdfBytes);
        input.setPdfBytes(pdfBytes);
        input.setPageCount(parseResult.pageCount());
        input.setExtractedText(parseResult.text());
        return input;
    }

    @Override
    public int order() {
        return 0;
    }
}
