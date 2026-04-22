package com.my.ai.cursor.knowledge.application.pojo.req;

public record IngestRequest(
    String title,
    String sourceUrl,
    String sourceOrg,
    String docType,
    String domain,
    String content
) {
}
