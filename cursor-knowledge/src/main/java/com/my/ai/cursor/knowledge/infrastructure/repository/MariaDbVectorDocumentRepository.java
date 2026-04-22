package com.my.ai.cursor.knowledge.infrastructure.repository;

import com.my.ai.cursor.ai.platform.application.VectorStoreRouter;
import com.my.ai.cursor.knowledge.domain.VectorDocumentRepository;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MariaDbVectorDocumentRepository implements VectorDocumentRepository {

    private final VectorStoreRouter vectorStoreRouter;

    public MariaDbVectorDocumentRepository(VectorStoreRouter vectorStoreRouter) {
        this.vectorStoreRouter = vectorStoreRouter;
    }

    @Override
    public void addAll(List<Document> documents) {
        vectorStoreRouter.route().add(documents);
    }
}
