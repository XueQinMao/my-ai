package com.my.ai.cursor.memory.infrastructure.repository.impl;

import com.my.ai.cursor.common.enums.MemoryStatus;
import com.my.ai.cursor.memory.infrastructure.entity.AgentMemory;
import com.my.ai.cursor.memory.domain.MemoryRepository;
import com.my.ai.cursor.memory.infrastructure.service.AgentMemoryService;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class MemoryRepositoryImpl implements MemoryRepository {

    private final AgentMemoryService agentMemoryService;

    public MemoryRepositoryImpl(AgentMemoryService agentMemoryService) {
        this.agentMemoryService = agentMemoryService;
    }

    @Override
    public Long save(AgentMemory item) {
        boolean success;
        if (item.getId() == null) {
            success = agentMemoryService.save(item);
        } else {
            success = agentMemoryService.updateById(item);
        }
        if (!success) {
            throw new IllegalStateException("Persist agent memory failed.");
        }
        return item.getId();
    }

    @Override
    public List<AgentMemory> findActiveByUserId(String userId, int limit) {
        LocalDateTime now = LocalDateTime.now();
        return agentMemoryService.lambdaQuery().eq(AgentMemory::getUserId, userId)
            .eq(AgentMemory::getStatus, MemoryStatus.ACTIVE.name())
            .and(wrapper -> wrapper.isNull(AgentMemory::getTtlAt).or().gt(AgentMemory::getTtlAt, now))
            .orderByDesc(AgentMemory::getImportance).orderByDesc(AgentMemory::getUpdatedAt).last("LIMIT " + limit)
            .list();
    }

    @Override
    @Deprecated
    public List<AgentMemory> searchRelevant(String userId, String query, int limit) {
        if (!StringUtils.hasText(query)) {
            return findActiveByUserId(userId, limit);
        }
        String trimmed = query.trim();
        LocalDateTime now = LocalDateTime.now();
        return agentMemoryService.lambdaQuery().eq(AgentMemory::getUserId, userId)
            .eq(AgentMemory::getStatus, MemoryStatus.ACTIVE.name())
            .and(wrapper -> wrapper.isNull(AgentMemory::getTtlAt).or().gt(AgentMemory::getTtlAt, now)).and(
                wrapper -> wrapper.like(AgentMemory::getContent, trimmed).or().like(AgentMemory::getSummary, trimmed)
                    .or().like(AgentMemory::getNormalizedKey, trimmed)).orderByDesc(AgentMemory::getImportance)
            .orderByDesc(AgentMemory::getUpdatedAt).last("LIMIT " + limit).list();
    }

    @Override
    public List<AgentMemory> query(com.my.ai.cursor.application.dto.memory.MemoryQueryRequest request) {
        LocalDateTime now = LocalDateTime.now();
        var query = agentMemoryService.lambdaQuery().eq(AgentMemory::getUserId, request.userId())
            .and(wrapper -> wrapper.isNull(AgentMemory::getTtlAt).or().gt(AgentMemory::getTtlAt, now))
            .orderByDesc(AgentMemory::getUpdatedAt);

        if (StringUtils.hasText(request.sessionId())) {
            query.eq(AgentMemory::getSessionId, request.sessionId());
        }
        if (StringUtils.hasText(request.type())) {
            query.eq(AgentMemory::getMemoryType, request.type());
        }
        if (StringUtils.hasText(request.status())) {
            query.eq(AgentMemory::getStatus, request.status());
        }
        return query.list();
    }

    @Override
    public void expire(String userId, Long id) {
        AgentMemory memory =
            agentMemoryService.lambdaQuery().eq(AgentMemory::getId, id).eq(AgentMemory::getUserId, userId)
                .last("LIMIT 1").one();
        if (memory == null) {
            return;
        }
        memory.setStatus(MemoryStatus.DELETED.name());
        memory.setUpdatedAt(LocalDateTime.now());
        agentMemoryService.updateById(memory);
    }

    @Override
    public List<AgentMemory> getAgentMemoryByVectorId(List<String> vectorIds) {

        return agentMemoryService.lambdaQuery().in(AgentMemory::getVectorStoreId, vectorIds).list();
    }
}
