package com.my.ai.cursor.knowledge.domain;


import com.my.ai.cursor.knowledge.application.pojo.dto.ChunkInsertModelDto;
import com.my.ai.cursor.knowledge.application.pojo.resp.KnowledgeSearchHit;

import java.util.List;
import java.util.Optional;

public interface KnowledgeRepository {

    Optional<Long> findDocumentIdByChecksum(String checksum);

    Long insertSourceDocument(String title, String sourceUrl, String sourceOrg, String docType, String checksum,
                              String status);

    void insertChunks(List<ChunkInsertModelDto> chunks);

    Optional<KnowledgeSearchHit> findHitByVectorDocId(String vectorDocId, Double score, String fallbackText);
}
