package com.my.ai.cursor.knowledge.application.ingestion.prepare;

import com.my.ai.cursor.common.utils.PdfUtils;
import com.my.ai.cursor.knowledge.application.strategy.TextExtractorStrategy;
import org.springframework.stereotype.Component;

@Component
public class PdfBoxTextExtractor implements TextExtractorStrategy {

    @Override
    public PdfUtils.PdfParseResult extract(byte[] pdfBytes) {
        return PdfUtils.extractText(pdfBytes);
    }
}
