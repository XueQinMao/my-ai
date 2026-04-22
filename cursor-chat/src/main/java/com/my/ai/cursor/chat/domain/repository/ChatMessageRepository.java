package com.my.ai.cursor.chat.domain.repository;


import com.my.ai.cursor.chat.infrastructure.entity.ChatMessage;

import java.util.List;

public interface ChatMessageRepository {

    Long saveMessage(ChatMessage message);

    List<ChatMessage> findRecentBySessionId(String sessionId, int limit);

    List<ChatMessage> findBySessionId(String sessionId, int pageNo, int pageSize);
}
