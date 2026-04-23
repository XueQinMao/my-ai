package com.my.ai.cursor.tool.knowledge;

public record KnowledgeToolHit(
    String title,
    String sourceUrl,
    String sourceOrg,
    String docType,
    String content,
    Double score
) {
}
