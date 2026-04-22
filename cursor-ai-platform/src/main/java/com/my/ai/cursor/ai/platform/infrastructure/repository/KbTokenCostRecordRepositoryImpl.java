package com.my.ai.cursor.ai.platform.infrastructure.repository;

import com.my.ai.cursor.ai.platform.domain.KbTokenCostRecordRepository;
import com.my.ai.cursor.ai.platform.infrastructure.entity.KbTokenCostRecord;
import com.my.ai.cursor.ai.platform.infrastructure.service.KbTokenCostRecordService;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * KbTokenCostRecordRepositoryImpl
 *
 * @author 刘强
 * @version 2026/04/17 16:25
 **/
@Repository
public class KbTokenCostRecordRepositoryImpl implements KbTokenCostRecordRepository {

    private final KbTokenCostRecordService kbTokenCostRecordService;

    public KbTokenCostRecordRepositoryImpl(KbTokenCostRecordService kbTokenCostRecordService) {
        this.kbTokenCostRecordService = kbTokenCostRecordService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insert(Long userId, Long chatId, String modelName, Integer tokenCount, Integer promptTokens, Integer completionTokens) {
        KbTokenCostRecord record = new KbTokenCostRecord();
        record.setUserId(userId);
        record.setChatId(chatId);
        record.setModelName(modelName);
        record.setTokenCount(tokenCount);
        record.setPromptTokens(promptTokens);
        record.setCompletionTokens(completionTokens);
        record.setCreatedAt(LocalDateTime.now());
        kbTokenCostRecordService.save(record);
    }
}
