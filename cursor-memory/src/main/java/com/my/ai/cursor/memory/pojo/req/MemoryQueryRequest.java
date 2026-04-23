package com.my.ai.cursor.memory.pojo.req;

public record MemoryQueryRequest(
    String userId,
    String sessionId,
    String type,
    String status
) {
}
