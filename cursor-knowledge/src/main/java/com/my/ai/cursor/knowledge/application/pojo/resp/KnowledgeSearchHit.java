package com.my.ai.cursor.knowledge.application.pojo.resp;

public record KnowledgeSearchHit(
    Long chunkId,
    Long documentId,
    Integer chunkNo,
    String title,
    String sourceUrl,
    String sourceOrg,
    String docType,
    String content,
    Double score,
    String vectorDocId
) {
}
