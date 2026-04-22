package com.my.ai.cursor.knowledge.application.pojo.req;

public record KnowledgeSearchRequest(
    String query,
    Integer topK,
    Double similarityThreshold
) {
}
