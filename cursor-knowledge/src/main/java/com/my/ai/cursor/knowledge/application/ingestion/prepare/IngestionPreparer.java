package com.my.ai.cursor.knowledge.application.ingestion.prepare;

import com.my.ai.cursor.knowledge.application.pojo.comtext.IngestionContext;
import com.my.ai.cursor.knowledge.application.pojo.req.IngestRequest;

/**
 * 负责将入库请求准备为可持久化的上下文。
 */
public interface IngestionPreparer {

    /**
     * 是否支持该文档类型。
     */
    boolean support(String type);

    /**
     * 准备入库上下文。
     */
    IngestionContext prepare(IngestRequest request) throws Exception;
}
