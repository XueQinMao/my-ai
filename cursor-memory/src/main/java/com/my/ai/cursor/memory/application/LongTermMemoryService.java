package com.my.ai.cursor.memory.application;

import com.my.ai.cursor.ai.platform.application.VectorStoreRouter;
import com.my.ai.cursor.ai.platform.application.observability.AiMetricsRecorder;
import com.my.ai.cursor.common.enums.AgentTaskType;
import com.my.ai.cursor.common.enums.AiScene;
import com.my.ai.cursor.memory.domain.MemoryRepository;
import com.my.ai.cursor.memory.infrastructure.entity.AgentMemory;
import com.my.ai.cursor.memory.pojo.dto.ChatMemoryWriteDto;
import com.my.ai.cursor.memory.pojo.dto.MemoryItemDto;
import com.my.ai.cursor.memory.pojo.req.MemoryDeleteRequest;
import com.my.ai.cursor.memory.pojo.req.MemoryQueryRequest;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LongTermMemoryService {

    private final MemoryRepository memoryRepository;
    private final MemoryExtractionService memoryExtractionService;

    @Resource
    private ApplicationContext applicationContext;

    private final VectorStoreRouter vectorStoreRouter;

    public LongTermMemoryService(MemoryRepository memoryRepository, MemoryExtractionService memoryExtractionService,
        VectorStoreRouter vectorStoreRouter) {
        this.memoryRepository = memoryRepository;
        this.memoryExtractionService = memoryExtractionService;
        this.vectorStoreRouter = vectorStoreRouter;
    }

    public void extractAndStore(String userId, String sessionId, Long sourceMessageId, String userMessage,
        String assistantMessage) {
        AiMetricsRecorder.recordMemoryAction(AiScene.MEMORY_EXTRACTION.name(), userId, sessionId,
            AgentTaskType.MEMORY_MAINTENANCE, "EXTRACT_REQUESTED", "STARTED", 0,
            "sourceMessageId=%s".formatted(sourceMessageId), null, null);
        List<AgentMemory> byUserId = memoryRepository.getByUserId(userId);
        Set<String> normalizedKeySet =
            Optional.ofNullable(byUserId).orElse(Collections.emptyList()).stream().map(AgentMemory::getNormalizedKey)
                .collect(Collectors.toSet());

        // 长期记忆不是原始消息直存，而是先提炼成可复用的记忆条目再落库。
        ChatMemoryWriteDto memoryWriteDto =
            new ChatMemoryWriteDto(userId, sessionId, sourceMessageId, userMessage, assistantMessage);
        List<AgentMemory> memories = memoryExtractionService.extract(memoryWriteDto, normalizedKeySet);
        AiMetricsRecorder.recordMemoryAction(AiScene.MEMORY_EXTRACTION.name(), userId, sessionId,
            AgentTaskType.MEMORY_MAINTENANCE, "EXTRACT_COMPLETED", "SUCCESS", memories.size(),
            "dedupBase=%s".formatted(normalizedKeySet.size()), null, null);
        //存向量 为了事务
        LongTermMemoryService bean = applicationContext.getBean(LongTermMemoryService.class);
        bean.store(memories);
    }

    @Transactional
    public void store(List<AgentMemory> memories) {
        if (memories.isEmpty()) {
            AiMetricsRecorder.recordMemoryAction(AiScene.MEMORY_EXTRACTION.name(), null, null,
                AgentTaskType.MEMORY_MAINTENANCE, "STORE_SKIPPED", "SKIPPED", 0, "No extracted memories", null, null);
            return;
        }
        vectorStoreRouter.route().add(memories.stream().map(convertToDocument()).toList());
        memories.forEach(memoryRepository::save);
        AgentMemory firstMemory = memories.getFirst();
        AiMetricsRecorder.recordMemoryAction(AiScene.MEMORY_EXTRACTION.name(), firstMemory.getUserId(),
            firstMemory.getSessionId(), AgentTaskType.MEMORY_MAINTENANCE, "STORE_COMPLETED", "SUCCESS",
            memories.size(), "memoryTypes=%s".formatted(memories.stream().map(AgentMemory::getMemoryType).distinct().toList()),
            null, null);
    }

    private Function<AgentMemory, Document> convertToDocument() {
        return memory -> {
            Map<String, Object> mateData =
                Map.of("vector_doc_id", memory.getVectorStoreId(), "userId", memory.getUserId(), "doc_type", "MEMORY");
            return new Document(memory.getVectorStoreId(), memory.getNormalizedKey(), mateData);
        };
    }

    public List<MemoryItemDto> query(MemoryQueryRequest request) {
        return memoryRepository.query(request).stream().map(this::toDto).toList();
    }

    public void forget(MemoryDeleteRequest request) {
        memoryRepository.expire(request.userId(), request.memoryId());
        AiMetricsRecorder.recordMemoryAction(AiScene.AGENT_CHAT.name(), request.userId(), null,
            AgentTaskType.MEMORY_MAINTENANCE, "FORGET", "SUCCESS", 1,
            "memoryId=%s".formatted(request.memoryId()), null, null);
    }

    private MemoryItemDto toDto(AgentMemory memory) {
        return new MemoryItemDto(memory.getId(), memory.getUserId(), memory.getSessionId(), memory.getMemoryType(),
            memory.getContent(), memory.getSummary(),
            memory.getImportance() == null ? null : memory.getImportance().doubleValue(),
            memory.getConfidence() == null ? null : memory.getConfidence().doubleValue(), memory.getStatus(),
            memory.getTtlAt());
    }
}
