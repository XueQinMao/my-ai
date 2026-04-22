package com.my.ai.cursor.knowledge.application.strategy;

import com.my.ai.cursor.common.utils.PdfUtils;

public interface TextExtractorStrategy {

    PdfUtils.PdfParseResult extract(byte[] pdfBytes);
}
