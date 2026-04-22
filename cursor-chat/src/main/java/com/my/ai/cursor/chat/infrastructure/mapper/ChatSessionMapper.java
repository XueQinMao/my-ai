package com.my.ai.cursor.chat.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.my.ai.cursor.chat.infrastructure.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
