package com.my.ai.cursor.chat.application.pojo.dto;

import java.time.LocalDateTime;

public record ChatMessageDto(
    Long id,
    String sessionId,
    String userId,
    String role,
    String content,
    LocalDateTime createdAt
) {
}
