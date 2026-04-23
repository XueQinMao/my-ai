package com.my.ai.cursor.common.port;

import java.util.List;

public interface ChatHistoryQueryPort {
    List<ChatHistoryItem> getRecentHistory(String sessionId, int limit);

    record ChatHistoryItem(Long id, String role, String content, String createdAt) {
    }
}
