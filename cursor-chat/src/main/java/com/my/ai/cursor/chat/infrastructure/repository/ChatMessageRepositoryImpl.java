package com.my.ai.cursor.chat.infrastructure.repository;

import com.my.ai.cursor.chat.domain.repository.ChatMessageRepository;
import com.my.ai.cursor.chat.infrastructure.entity.ChatMessage;
import com.my.ai.cursor.chat.infrastructure.service.ChatMessageDbService;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ChatMessageRepositoryImpl implements ChatMessageRepository {

    private final ChatMessageDbService chatMessageDbService;

    public ChatMessageRepositoryImpl(ChatMessageDbService chatMessageDbService) {
        this.chatMessageDbService = chatMessageDbService;
    }

    @Override
    public Long saveMessage(ChatMessage message) {
        if (!chatMessageDbService.save(message)) {
            throw new IllegalStateException("Insert chat message failed.");
        }
        return message.getId();
    }

    @Override
    public List<ChatMessage> findRecentBySessionId(String sessionId, int limit) {
        return chatMessageDbService.lambdaQuery()
            .eq(ChatMessage::getSessionId, sessionId)
            .orderByDesc(ChatMessage::getCreatedAt)
            .last("LIMIT " + limit)
            .list()
            .stream()
            .toList();
    }

    @Override
    public List<ChatMessage> findBySessionId(String sessionId, int pageNo, int pageSize) {
        int safePageNo = Math.max(pageNo, 1);
        int safePageSize = Math.max(pageSize, 1);
        int offset = (safePageNo - 1) * safePageSize;
        return chatMessageDbService.lambdaQuery()
            .eq(ChatMessage::getSessionId, sessionId)
            .orderByAsc(ChatMessage::getCreatedAt)
            .last("LIMIT " + offset + "," + safePageSize)
            .list();
    }
}
