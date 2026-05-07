package com.my.ai.cursor.knowledge.application;

import com.my.ai.cursor.ai.platform.application.AiGatewayService;
import com.my.ai.cursor.ai.platform.application.observability.AiMetricsRecorder;
import com.my.ai.cursor.common.enums.AgentTaskType;
import com.my.ai.cursor.common.enums.AiScene;
import com.my.ai.cursor.knowledge.application.pojo.req.KnowledgeSearchRequest;
import com.my.ai.cursor.knowledge.application.pojo.resp.KnowledgeSearchHit;
import com.my.ai.cursor.knowledge.domain.KnowledgeRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class KnowledgeSearchService {

    private final AiGatewayService aiGatewayService;
    private final KnowledgeRepository knowledgeRepository;

    public KnowledgeSearchService(AiGatewayService aiGatewayService, KnowledgeRepository knowledgeRepository) {
        this.aiGatewayService = aiGatewayService;
        this.knowledgeRepository = knowledgeRepository;
    }

    public List<KnowledgeSearchHit> search(KnowledgeSearchRequest request) {
        if (!StringUtils.hasText(request.query())) {
            throw new IllegalArgumentException("query cannot be empty");
        }
        long startedAtNanos = System.nanoTime();
        int topK = request.topK() == null || request.topK() <= 0 ? 5 : request.topK();
        Double similarityThreshold = Objects.isNull(request.similarityThreshold()) ? 0.7 : request.similarityThreshold();
        try {
            SearchRequest.Builder builder = SearchRequest.builder().query(request.query()).topK(topK)
                .similarityThreshold(similarityThreshold);
            List<Document> docs = aiGatewayService.similaritySearch(builder.build());
            List<KnowledgeSearchHit> hits = docs.stream().map(this::convertToHit).toList();
            int hitCount = hits.size();
            AiMetricsRecorder.recordRagRetrieve(AiScene.AGENT_CHAT.name(), "SUCCESS", elapsedMs(startedAtNanos), hitCount);
            AiMetricsRecorder.recordKnowledgeAction(AiScene.AGENT_CHAT.name(), null, null, AgentTaskType.FACT_ANSWER,
                "SEARCH", "SUCCESS", elapsedMs(startedAtNanos), hitCount, similarityThreshold, request.query());
            return hits;
        } catch (Exception e) {
            AiMetricsRecorder.recordRagRetrieve(AiScene.AGENT_CHAT.name(), "FAILED", elapsedMs(startedAtNanos), 0);
            AiMetricsRecorder.recordKnowledgeAction(AiScene.AGENT_CHAT.name(), null, null, AgentTaskType.FACT_ANSWER,
                "SEARCH", "FAILED", elapsedMs(startedAtNanos), 0, similarityThreshold, request.query());
            throw e;
        }
    }

    private KnowledgeSearchHit convertToHit(Document doc) {
        Map<String, Object> metadata = doc.getMetadata();
        Double score = doc.getScore();
        String vectorDocId = asString(metadata.get("vector_doc_id"));

        if (StringUtils.hasText(vectorDocId)) {
            return knowledgeRepository.findHitByVectorDocId(vectorDocId, score, doc.getText())
                .orElse(createDefaultHit(metadata, doc.getText(), score, vectorDocId));
        }

        return createDefaultHit(metadata, doc.getText(), score, null);
    }

    private KnowledgeSearchHit createDefaultHit(Map<String, Object> metadata, String content, Double score,
        String vectorDocId) {
        return new KnowledgeSearchHit(null, null, null, asString(metadata.get("title")),
            asString(metadata.get("source_url")), asString(metadata.get("source_org")),
            asString(metadata.get("doc_type")), content, score, vectorDocId);
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private long elapsedMs(long startedAtNanos) {
        return (System.nanoTime() - startedAtNanos) / 1_000_000L;
    }
}
