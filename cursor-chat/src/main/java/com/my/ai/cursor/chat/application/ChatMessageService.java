package com.my.ai.cursor.chat.application;

import com.my.ai.cursor.chat.application.pojo.dto.ChatMessageDto;
import com.my.ai.cursor.chat.application.pojo.req.ChatHistoryQueryRequest;
import com.my.ai.cursor.chat.application.pojo.req.ChatRequest;
import com.my.ai.cursor.chat.domain.repository.ChatMessageRepository;
import com.my.ai.cursor.chat.domain.repository.ChatSessionRepository;
import com.my.ai.cursor.chat.infrastructure.entity.ChatMessage;
import com.my.ai.cursor.chat.infrastructure.entity.ChatSession;
import com.my.ai.cursor.common.enums.ChatMessageRole;
import com.my.ai.cursor.common.enums.ChatSessionStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ChatMessageDbService
 *
 * @author 刘强
 * @version 2026/04/22 14:01
 **/
@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    private final ChatSessionRepository chatSessionRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository,
        ChatSessionRepository chatSessionRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatSessionRepository = chatSessionRepository;
    }

    public List<ChatMessageDto> history(ChatHistoryQueryRequest request) {
        return chatMessageRepository.findBySessionId(request.sessionId(), request.pageNo(), request.pageSize()).stream()
            .map(message -> new ChatMessageDto(message.getId(), message.getSessionId(), message.getUserId(),
                message.getRole(), message.getContent(), message.getCreatedAt())).toList();
    }


    public void checkOrCreateChatSession(ChatRequest request) {
        // 业务会话主键与 Spring AI conversationId 对齐，后续短期记忆直接复用 sessionId。
        chatSessionRepository.findBySessionId(request.sessionId()).orElseGet(() -> createSession(request));
    }

    public Long saveUserMessage(ChatRequest request) {
        return saveMessage(request.sessionId(), request.userId(), ChatMessageRole.USER.name(), request.message(), null,
            null, null);
    }

    public Long saveAssistantMessage(ChatRequest request, String content) {
        return saveMessage(request.sessionId(), request.userId(), ChatMessageRole.ASSISTANT.name(), content, null, null,
            null);
    }

    private ChatSession createSession(ChatRequest request) {
        LocalDateTime now = LocalDateTime.now();
        ChatSession session = new ChatSession();
        session.setSessionId(request.sessionId());
        session.setUserId(request.userId());
        session.setScene(request.scene());
        session.setStatus(ChatSessionStatus.ACTIVE.name());
        session.setTitle(buildTitle(request.message()));
        session.setLastMessageAt(now);
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        chatSessionRepository.saveSession(session);
        return session;
    }

    private Long saveMessage(String sessionId, String userId, String role, String content, Integer tokensInput,
        Integer tokensOutput, String traceId) {
        // messageIndex 由业务侧维护，保证历史回放和长期记忆引用都有稳定顺序。
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setUserId(userId);
        message.setRole(role);
        message.setContent(content);
        message.setTokensInput(tokensInput);
        message.setTokensOutput(tokensOutput);
        message.setTraceId(traceId);
        message.setCreatedAt(LocalDateTime.now());
        Long messageId = chatMessageRepository.saveMessage(message);
        chatSessionRepository.touchSession(sessionId);
        return messageId;
    }

    private String buildTitle(String message) {
        // 会话标题先用首条消息截断生成，后续如有需要再做 LLM 摘要优化。
        String text = message.trim();
        return text.length() <= 20 ? text : text.substring(0, 20);
    }
}
