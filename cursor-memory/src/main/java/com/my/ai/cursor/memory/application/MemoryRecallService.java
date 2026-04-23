package com.my.ai.cursor.memory.application;

import com.my.ai.cursor.ai.platform.application.AiGatewayService;
import com.my.ai.cursor.memory.application.config.AppMemoryProperties;
import com.my.ai.cursor.memory.domain.MemoryRepository;
import com.my.ai.cursor.memory.pojo.dto.MemoryItemDto;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MemoryRecallService {

    private final MemoryRepository memoryRepository;
    private final AppMemoryProperties appMemoryProperties;
    private final AiGatewayService aiGatewayService;

    public MemoryRecallService(MemoryRepository memoryRepository, AppMemoryProperties appMemoryProperties,
        AiGatewayService aiGatewayService) {
        this.memoryRepository = memoryRepository;
        this.appMemoryProperties = appMemoryProperties;
        this.aiGatewayService = aiGatewayService;
    }

    public List<MemoryItemDto> recall(String userId, String query, Integer limit) {
        if (!appMemoryProperties.getLongTerm().isEnabled() || !StringUtils.hasText(userId)) {
            return List.of();
        }
        int recallLimit = limit == null || limit <= 0 ? appMemoryProperties.getLongTerm().getRecallLimit() : limit;
        //提取考虑从向量数据库中去提取
        SearchRequest build = SearchRequest.builder().query(query).topK(recallLimit).similarityThreshold(0.7)
            .filterExpression("userId == '" + escapeLiteral(userId) + "' AND doc_type == 'MEMORY'").build();
        List<Document> documents = aiGatewayService.similaritySearch(build);
        return documents.stream().map(this::toDto).toList();
    }

    private MemoryItemDto toDto(Document document) {
        return new MemoryItemDto(null, null, null, null, document.getText(), null, null, null, null, null);
    }

    private String escapeLiteral(String value) {
        return value.replace("'", "\\'");
    }
}
