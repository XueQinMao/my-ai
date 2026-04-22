package com.my.ai.cursor.memory.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.my.ai.cursor.memory.infrastructure.entity.AgentMemory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentMemoryMapper extends BaseMapper<AgentMemory> {
}
