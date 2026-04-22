package com.my.ai.cursor.memory.infrastructure.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.my.ai.cursor.memory.infrastructure.entity.AgentMemory;
import com.my.ai.cursor.memory.infrastructure.mapper.AgentMemoryMapper;
import org.springframework.stereotype.Service;

@Service
public class AgentMemoryService extends ServiceImpl<AgentMemoryMapper, AgentMemory> {
}
