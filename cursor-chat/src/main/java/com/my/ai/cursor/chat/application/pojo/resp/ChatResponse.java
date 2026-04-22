package com.my.ai.cursor.chat.application.pojo.resp;

public record ChatResponse(
    String userId,
    String sessionId,
    String scene,
    String content,
    boolean knowledgeEnabled,
    boolean longTermMemoryEnabled
) {
}
