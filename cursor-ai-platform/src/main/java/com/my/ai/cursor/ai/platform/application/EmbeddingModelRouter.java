package com.my.ai.cursor.ai.platform.application;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingModelRouter {

    private final EmbeddingModel embeddingModel;

    public EmbeddingModelRouter(@Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public EmbeddingModel route() {
        return embeddingModel;
    }
}
