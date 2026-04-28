package com.my.ai.cursor.memory.pojo.dto;

public record ChatMemoryWriteDto(
    String userId,
    String sessionId,
    Long sourceMessageId,
    String userMessage,
    String assistantMessage
) {
}
