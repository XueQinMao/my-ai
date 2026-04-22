package com.my.ai.cursor.knowledge.application.strategy;

import com.my.ai.cursor.common.utils.PdfUtils;
import com.my.ai.cursor.knowledge.application.cleaning.TextCorrectionService;
import com.my.ai.cursor.knowledge.application.pojo.dto.ChatCleanDto;
import com.my.ai.cursor.knowledge.application.pojo.req.IngestRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DefaultTextCleanerStrategy implements TextCleanerStrategy {

    private static final Logger log = LoggerFactory.getLogger(DefaultTextCleanerStrategy.class);

    private final TextCorrectionService textCorrectionService;

    public DefaultTextCleanerStrategy(TextCorrectionService textCorrectionService) {
        this.textCorrectionService = textCorrectionService;
    }

    @Override
    public String clean(String text, IngestRequest request) {
        String normalized = PdfUtils.cleanChineseText(text);
        if (!StringUtils.hasText(normalized)) {
            return normalized;
        }

        String domainName = request.domain().trim();
        if (!StringUtils.hasText(domainName)) {
            return normalized;
        }

        try {
            ChatCleanDto chatResult = textCorrectionService.correct(normalized, domainName);
            if (chatResult == null) {
                return normalized;
            }
            String cleaned = normalized;
            for (ChatCleanDto.Error error : chatResult.getErrors()) {
                if (error.getIncorrect() == null || error.getCorrect() == null) {
                    continue;
                }
                cleaned = cleaned.replace(error.getIncorrect(), error.getCorrect());
            }
            return cleaned;
        } catch (Exception ex) {
            log.warn("Failed to clean text with LLM, fallback to rules only: {}", ex.getMessage());
            ex.printStackTrace();
           throw ex;
        }
    }
}
