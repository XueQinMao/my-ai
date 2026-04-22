package com.my.ai.cursor.knowledge.application;

import com.my.ai.cursor.ai.platform.application.AiGatewayService;
import com.my.ai.cursor.knowledge.application.pojo.req.KnowledgeSearchRequest;
import com.my.ai.cursor.knowledge.application.pojo.resp.KnowledgeSearchHit;
import com.my.ai.cursor.knowledge.domain.KnowledgeRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
        int topK = request.topK() == null || request.topK() <= 0 ? 5 : request.topK();
        SearchRequest.Builder builder = SearchRequest.builder().query(request.query()).topK(topK)
            .similarityThreshold(Objects.isNull(request.similarityThreshold()) ? 0.7 : request.similarityThreshold());
        //向量查询
        List<Document> docs = aiGatewayService.similaritySearch(builder.build());
        return docs.stream().map(this::convertToHit).toList();
    }

    public String generateRagContext(String query, int topK) {
        List<KnowledgeSearchHit> hits = search(new KnowledgeSearchRequest(query, topK, null));

        AtomicInteger index = new AtomicInteger(1);
        return Optional.ofNullable(hits).orElse(Collections.emptyList()).stream().map(hit -> formatHit(hit, index))
            .collect(Collectors.joining("\n\n"));
    }

    private String formatHit(KnowledgeSearchHit hit, AtomicInteger index) {
        StringBuilder sb = new StringBuilder();
        sb.append("片段").append(index.getAndIncrement()).append("：")
            .append(hit.title() == null ? "unknown" : hit.title())
            .append('\n');

        if (StringUtils.hasText(hit.sourceUrl())) {
            sb.append("来源URL: ").append(hit.sourceUrl()).append('\n');
        }

        sb.append(hit.content());
        return sb.toString();
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
}
