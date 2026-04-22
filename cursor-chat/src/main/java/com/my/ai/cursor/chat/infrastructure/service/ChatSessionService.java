package com.my.ai.cursor.chat.infrastructure.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.my.ai.cursor.chat.infrastructure.entity.ChatSession;
import com.my.ai.cursor.chat.infrastructure.mapper.ChatSessionMapper;
import org.springframework.stereotype.Service;

@Service
public class ChatSessionService extends ServiceImpl<ChatSessionMapper, ChatSession> {
}
