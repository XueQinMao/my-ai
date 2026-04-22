package com.my.ai.cursor.knowledge.application.ingestion.prepare;

import com.my.ai.cursor.knowledge.application.pojo.comtext.IngestionContext;
import com.my.ai.cursor.knowledge.application.pojo.req.IngestRequest;

/**
 * IPrepareService
 *
 * @author 刘强
 * @version 2026/04/15 17:13
 **/
public interface IPrepareService {

    /**
     * 是否支持该类型
     * @param type
     * @return
     */
    Boolean support(String type);

    /**
     * 解析文本
     * @param request
     * @return
     * @throws Exception
     */
    IngestionContext prepare(IngestRequest request) throws Exception;
}
