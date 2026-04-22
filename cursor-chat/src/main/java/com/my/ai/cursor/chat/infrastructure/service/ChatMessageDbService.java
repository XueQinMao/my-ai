package com.my.ai.cursor.chat.infrastructure.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.my.ai.cursor.chat.infrastructure.entity.ChatMessage;
import com.my.ai.cursor.chat.infrastructure.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageDbService extends ServiceImpl<ChatMessageMapper, ChatMessage> {
}
