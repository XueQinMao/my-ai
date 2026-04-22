package com.my.ai.cursor.memory.domain;


import com.my.ai.cursor.memory.infrastructure.entity.AgentMemory;

import java.util.List;

public interface MemoryRepository {

    Long save(AgentMemory item);

    List<AgentMemory> findActiveByUserId(String userId, int limit);

    List<AgentMemory> searchRelevant(String userId, String query, int limit);

    List<AgentMemory> query(com.my.ai.cursor.application.dto.memory.MemoryQueryRequest request);

    void expire(String userId, Long id);

    List<AgentMemory> getAgentMemoryByVectorId(List<String> vectorIds);
}
