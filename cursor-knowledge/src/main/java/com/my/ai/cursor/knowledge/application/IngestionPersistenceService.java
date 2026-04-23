package com.my.ai.cursor.knowledge.application;

import com.alibaba.fastjson.JSON;
import com.my.ai.cursor.knowledge.application.pojo.comtext.IngestionContext;
import com.my.ai.cursor.knowledge.application.pojo.dto.ChunkInsertModelDto;
import com.my.ai.cursor.knowledge.application.pojo.dto.PreparedChunkDto;
import com.my.ai.cursor.knowledge.domain.KnowledgeRepository;
import com.my.ai.cursor.knowledge.domain.VectorDocumentRepository;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IngestionPersistenceService {

    private static final String STATUS_READY = "READY";

    private final KnowledgeRepository knowledgeRepository;
    private final VectorDocumentRepository vectorDocumentRepository;

    public IngestionPersistenceService(KnowledgeRepository knowledgeRepository,
                                       VectorDocumentRepository vectorDocumentRepository) {
        this.knowledgeRepository = knowledgeRepository;
        this.vectorDocumentRepository = vectorDocumentRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long persist(IngestionContext context) {
        Long documentId = knowledgeRepository.insertSourceDocument(
            context.getTitle(),
            context.getRequest().sourceUrl(),
            context.getRequest().sourceOrg(),
            context.getRequest().docType(),
            context.getChecksum(),
            STATUS_READY
        );

        List<ChunkInsertModelDto> chunkInsertModels = context.getChunks()
            .stream()
            .map(chunk -> buildChunkInsertModel(documentId, chunk, context))
            .toList();
        knowledgeRepository.insertChunks(chunkInsertModels);
        vectorDocumentRepository.addAll(toVectorDocuments(chunkInsertModels, context));
        return documentId;
    }

    private ChunkInsertModelDto buildChunkInsertModel(Long documentId, PreparedChunkDto chunk, IngestionContext context) {
        return ChunkInsertModelDto.of(
            documentId,
            chunk.chunkNo(),
            chunk.chunkText(),
            chunk.tokenCount(),
            chunk.vectorDocId(),
            JSON.toJSONString(buildMetadata(documentId, chunk, context))
        );
    }

    private List<Document> toVectorDocuments(List<ChunkInsertModelDto> chunkInsertModels, IngestionContext context) {
        return chunkInsertModels.stream()
            .map(chunk -> new Document(chunk.getVectorDocId(), chunk.getChunkText(), buildMetadata(chunk.getDocumentId(), chunk, context)))
            .collect(Collectors.toList());
    }

    private Map<String, Object> buildMetadata(Long documentId, PreparedChunkDto chunk, IngestionContext context) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("vector_doc_id", chunk.vectorDocId());
        metadata.put("document_id", documentId);
        metadata.put("chunk_no", chunk.chunkNo());
        metadata.put("source_org", context.getRequest().sourceOrg());
        metadata.put("doc_type", context.getRequest().docType());
        metadata.put("title", context.getTitle());
        metadata.put("source_url", context.getRequest().sourceUrl());
        return metadata;
    }

    private Map<String, Object> buildMetadata(Long documentId, ChunkInsertModelDto chunk, IngestionContext context) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("vector_doc_id", chunk.getVectorDocId());
        metadata.put("document_id", documentId);
        metadata.put("chunk_no", chunk.getChunkNo());
        metadata.put("source_org", context.getRequest().sourceOrg());
        metadata.put("doc_type", context.getRequest().docType());
        metadata.put("title", context.getTitle());
        metadata.put("source_url", context.getRequest().sourceUrl());
        return metadata;
    }
}
