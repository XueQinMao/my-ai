package com.my.ai.cursor.knowledge.application.strategy;

public interface DocumentFetcherStrategy {

    byte[] fetch(String sourceUrl);
}
