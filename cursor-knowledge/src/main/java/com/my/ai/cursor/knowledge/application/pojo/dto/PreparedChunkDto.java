package com.my.ai.cursor.knowledge.application.pojo.dto;

public record PreparedChunkDto(
    int chunkNo,
    String chunkText,
    int tokenCount,
    String vectorDocId
) {
}
