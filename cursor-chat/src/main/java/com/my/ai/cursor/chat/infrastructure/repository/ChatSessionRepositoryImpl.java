package com.my.ai.cursor.chat.infrastructure.repository;

import com.my.ai.cursor.chat.domain.repository.ChatSessionRepository;
import com.my.ai.cursor.chat.infrastructure.entity.ChatSession;
import com.my.ai.cursor.chat.infrastructure.service.ChatSessionService;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class ChatSessionRepositoryImpl implements ChatSessionRepository {

    private final ChatSessionService chatSessionService;

    public ChatSessionRepositoryImpl(ChatSessionService chatSessionService) {
        this.chatSessionService = chatSessionService;
    }

    @Override
    public Long saveSession(ChatSession session) {
        boolean saved = chatSessionService.save(session);
        if (!saved) {
            throw new IllegalStateException("Insert chat session failed.");
        }
        return session.getId();
    }

    @Override
    public Optional<ChatSession> findBySessionId(String sessionId) {
        return Optional.ofNullable(
            chatSessionService.lambdaQuery()
                .eq(ChatSession::getSessionId, sessionId)
                .last("LIMIT 1")
                .one()
        );
    }

    @Override
    public void touchSession(String sessionId) {
        ChatSession session = findBySessionId(sessionId)
            .orElseThrow(() -> new IllegalStateException("Chat session not found, sessionId=" + sessionId));
        LocalDateTime now = LocalDateTime.now();
        session.setLastMessageAt(now);
        session.setUpdatedAt(now);
        if (!chatSessionService.updateById(session)) {
            throw new IllegalStateException("Update chat session failed, sessionId=" + sessionId);
        }
    }
}
