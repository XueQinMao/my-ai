package com.my.ai.cursor.memory.domain;

import com.my.ai.cursor.memory.infrastructure.entity.AgentMemory;
import com.my.ai.cursor.memory.pojo.req.MemoryQueryRequest;

import java.util.List;

public interface MemoryRepository {

    Long save(AgentMemory item);

    List<AgentMemory> query(MemoryQueryRequest request);

    void expire(String userId, Long id);

    List<AgentMemory> getByUserId(String userId);
}
