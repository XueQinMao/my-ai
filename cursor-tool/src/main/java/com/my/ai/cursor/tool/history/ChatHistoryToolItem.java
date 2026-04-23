package com.my.ai.cursor.tool.history;

import java.time.LocalDateTime;

public record ChatHistoryToolItem(Long id, String role, String content, LocalDateTime createdAt) {
}
