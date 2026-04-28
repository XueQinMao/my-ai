package com.my.ai.cursor.memory.pojo.req;

public record MemoryDeleteRequest(
    String userId,
    Long memoryId
) {
}
