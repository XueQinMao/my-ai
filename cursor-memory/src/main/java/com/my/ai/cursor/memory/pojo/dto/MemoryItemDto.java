package com.my.ai.cursor.memory.pojo.dto;

import java.time.LocalDateTime;

public record MemoryItemDto(
    Long id,
    String userId,
    String sessionId,
    String memoryType,
    String content,
    String summary,
    Double importance,
    Double confidence,
    String status,
    LocalDateTime ttlAt) {
}
