package com.my.ai.cursor.chat.domain.repository;

import com.my.ai.cursor.chat.infrastructure.entity.ChatSession;

import java.util.Optional;

/**
 * ChatSessionRepository
 *
 * @author 刘强
 * @version 2026/04/22 14:09
 **/
public interface ChatSessionRepository {

    Long saveSession(ChatSession session);

    Optional<ChatSession> findBySessionId(String sessionId);

    void touchSession(String sessionId);
}
