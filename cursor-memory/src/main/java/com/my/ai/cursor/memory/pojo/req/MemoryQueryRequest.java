package com.my.ai.cursor.application.dto.memory;

public record MemoryQueryRequest(
    String userId,
    String sessionId,
    String type,
    String status
) {
}
