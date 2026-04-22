package com.my.ai.cursor.application.dto.memory;

public record MemoryDeleteRequest(
    String userId,
    Long memoryId
) {
}
