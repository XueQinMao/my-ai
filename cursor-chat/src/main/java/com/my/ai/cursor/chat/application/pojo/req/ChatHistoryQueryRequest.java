package com.my.ai.cursor.chat.application.pojo.req;

public record ChatHistoryQueryRequest(
    String sessionId,
    Integer pageNo,
    Integer pageSize
) {
}
