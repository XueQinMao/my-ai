package com.my.ai.cursor.knowledge.application.strategy;

import com.my.ai.cursor.knowledge.application.pojo.req.IngestRequest;

public interface TextCleanerStrategy {

    String clean(String text, IngestRequest request);
}
