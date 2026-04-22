package com.my.ai.cursor.ai.platform.domain;

/**
 * KbTokenCostRecordRepositoryImpl
 *
 * @author 刘强
 * @version 2026/04/17 16:25
 **/
public interface KbTokenCostRecordRepository {

    void insert(Long userId, Long chatId, String modelName, Integer tokenCount, Integer promptTokens, Integer completionTokens);
}
