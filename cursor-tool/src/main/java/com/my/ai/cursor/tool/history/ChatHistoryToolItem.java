package com.my.ai.cursor.tool.history;

public record ChatHistoryToolItem(
    Long id,
    String role,
    String content,
    String createdAt
) {
}
