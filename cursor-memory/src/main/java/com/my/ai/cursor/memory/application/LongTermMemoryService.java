package com.my.ai.cursor.memory.application;

import com.my.ai.cursor.ai.platform.application.VectorStoreRouter;
import com.my.ai.cursor.memory.infrastructure.entity.AgentMemory;
import com.my.ai.cursor.memory.domain.MemoryRepository;
import com.my.ai.cursor.memory.pojo.dto.ChatMemoryWriteCommand;
import com.my.ai.cursor.memory.pojo.dto.MemoryItemDto;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        // 长期记忆不是原始消息直存，而是先提炼成可复用的记忆条目再落库。
        ChatMemoryWriteCommand command =
            new ChatMemoryWriteCommand(userId, sessionId, sourceMessageId, userMessage, assistantMessage);
        List<AgentMemory> memories = memoryExtractionService.extract(command);
        //存向量 为了事务
        LongTermMemoryService bean = applicationContext.getBean(LongTermMemoryService.class);
        bean.store(memories);
    }

    @Transactional
    public void store(List<AgentMemory> memories) {
        vectorStoreRouter.route().add(memories.stream().map(convertToDocument()).toList());
        memories.forEach(memoryRepository::save);
    }

    private Function<AgentMemory, Document> convertToDocument() {
        return memory -> {
            Map<String, Object> mateData =
                Map.of("vector_doc_id", memory.getVectorStoreId(), "userId", memory.getUserId(), "doc_type", "MEMORY");
            return new Document(memory.getVectorStoreId(), memory.getNormalizedKey(), mateData);
        };
    }

    public List<MemoryItemDto> query(com.my.ai.cursor.application.dto.memory.MemoryQueryRequest request) {
        return memoryRepository.query(request).stream().map(this::toDto).toList();
    }

    public void forget(com.my.ai.cursor.application.dto.memory.MemoryDeleteRequest request) {
        memoryRepository.expire(request.userId(), request.memoryId());
    }

    private MemoryItemDto toDto(AgentMemory memory) {
        return new MemoryItemDto(memory.getId(), memory.getUserId(), memory.getSessionId(), memory.getMemoryType(),
            memory.getContent(), memory.getSummary(),
            memory.getImportance() == null ? null : memory.getImportance().doubleValue(),
            memory.getConfidence() == null ? null : memory.getConfidence().doubleValue(), memory.getStatus(),
            memory.getTtlAt());
    }
}
