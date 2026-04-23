package com.my.ai.cursor.chat.application.pojo.req;

public record ChatRequest(String userId, String sessionId, String message, String scene, Boolean enableKnowledge,
                          Boolean enableLongTermMemory, Integer memoryWindow) {
}
