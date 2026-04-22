package com.my.ai.cursor.knowledge.infrastructure.repository;

import com.my.ai.cursor.knowledge.application.pojo.dto.ChunkInsertModelDto;
import com.my.ai.cursor.knowledge.application.pojo.resp.KnowledgeSearchHit;
import com.my.ai.cursor.knowledge.domain.KnowledgeRepository;
import com.my.ai.cursor.knowledge.infrastructure.entity.KbDocumentChunk;
import com.my.ai.cursor.knowledge.infrastructure.entity.KbSourceDocument;
import com.my.ai.cursor.knowledge.infrastructure.service.KbDocumentChunkService;
import com.my.ai.cursor.knowledge.infrastructure.service.KbSourceDocumentService;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class KnowledgeRepositoryImpl implements KnowledgeRepository {

    private final KbDocumentChunkService kbDocumentChunkService;
    private final KbSourceDocumentService kbSourceDocumentService;

    public KnowledgeRepositoryImpl(KbDocumentChunkService kbDocumentChunkService,
        KbSourceDocumentService kbSourceDocumentService) {
        this.kbDocumentChunkService = kbDocumentChunkService;
        this.kbSourceDocumentService = kbSourceDocumentService;
    }

    @Override
    public Optional<Long> findDocumentIdByChecksum(String checksum) {
        var document =
            kbSourceDocumentService.lambdaQuery().eq(KbSourceDocument::getChecksum, checksum).last("LIMIT 1").select(KbSourceDocument::getId).one();
        return Optional.ofNullable(document).map(KbSourceDocument::getId);
    }

    @Override
    public Long insertSourceDocument(String title, String sourceUrl, String sourceOrg, String docType,
                                     String checksum, String status) {
        KbSourceDocument document = new KbSourceDocument();
        document.setTitle(title);
        document.setSourceUrl(sourceUrl);
        document.setSourceOrg(sourceOrg);
        document.setDocType(docType);
        document.setChecksum(checksum);
        document.setStatus(status);
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());

        boolean b = kbSourceDocumentService.save(document);
        if (!b) {
            throw new IllegalStateException("Insert source document failed: no generated key returned.");
        }
        return document.getId();
    }

    @Override
    public void insertChunks(List<ChunkInsertModelDto> chunks) {
        List<KbDocumentChunk> entities = chunks.stream().map(chunk -> {
            KbDocumentChunk entity = new KbDocumentChunk();
            entity.setDocumentId(chunk.getDocumentId());
            entity.setChunkNo(chunk.getChunkNo());
            entity.setChunkText(chunk.getChunkText());
            entity.setTokenCount(chunk.getTokenCount());
            entity.setVectorDocId(chunk.getVectorDocId());
            entity.setMetadataJson(chunk.getMetadataJson());
            entity.setCreatedAt(LocalDateTime.now());
            return entity;
        }).toList();

        boolean b = kbDocumentChunkService.saveBatch(entities);
        if (!b) {
            throw new IllegalStateException("Insert document chunk failed.");
        }
    }

    @Override
    public Optional<KnowledgeSearchHit> findHitByVectorDocId(String vectorDocId, Double score, String fallbackText) {
        var document = kbDocumentChunkService.getBaseMapper().selectHitByVectorDocId(vectorDocId);
        if (Objects.isNull(document)) {
            return Optional.empty();
        }
        return Optional.of(
            new KnowledgeSearchHit(document.getChunkId(), document.getDocumentId(), document.getChunkNo(),
                document.getTitle(), document.getSourceUrl(), document.getSourceOrg(), document.getDocType(),
                document.getContent(), score, vectorDocId));
    }
}
