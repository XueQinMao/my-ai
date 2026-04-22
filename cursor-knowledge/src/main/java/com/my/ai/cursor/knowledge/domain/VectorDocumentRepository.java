package com.my.ai.cursor.knowledge.domain;

import org.springframework.ai.document.Document;

import java.util.List;

public interface VectorDocumentRepository {

    void addAll(List<Document> documents);
}
