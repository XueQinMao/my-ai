package com.my.ai.cursor.ai.platform.application;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

@Component
public class VectorStoreRouter {

    private final VectorStore vectorStore;

    public VectorStoreRouter(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public VectorStore route() {
        return vectorStore;
    }
}
