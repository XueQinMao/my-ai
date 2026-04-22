package com.my.ai.cursor.knowledge.application.pojo.dto;

import lombok.Data;

/**
 * ChunkInsertModelDto
 *
 * @author 刘强
 * @version 2026/04/14 17:40
 **/
@Data
public class ChunkInsertModelDto {

    private Long documentId;
    private int chunkNo;
    private String chunkText;
    private int tokenCount;
    private String vectorDocId;
    private String metadataJson;

    public static ChunkInsertModelDto of(Long documentId, int chunkNo, String chunkText, int tokenCount,
        String vectorDocId, String metadataJson) {
        ChunkInsertModelDto dto = new ChunkInsertModelDto();
        dto.setDocumentId(documentId);
        dto.setChunkNo(chunkNo);
        dto.setChunkText(chunkText);
        dto.setTokenCount(tokenCount);
        dto.setVectorDocId(vectorDocId);
        dto.setMetadataJson(metadataJson);
        return dto;
    }
}
