package com.my.ai.cursor.chat.application;

import com.my.ai.cursor.ai.platform.application.AiGatewayService;
import com.my.ai.cursor.common.enums.AiScene;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

@Service
@Deprecated
public class ChatService {

    private final AiGatewayService aiGatewayService;

    public ChatService(AiGatewayService aiGatewayService) {
        this.aiGatewayService = aiGatewayService;
    }

    public String chat(String message) {
        return aiGatewayService.chat(AiScene.NORMAL_CHAT, message);
    }

    public Flux<String> streamChat(String message) {
        return aiGatewayService.streamChat(AiScene.NORMAL_CHAT, message);
    }

    public Flux<String> streamChatWithContext(String message, String context) {
        if (context == null || context.isBlank()) {
            return streamChat(message);
        }

        var promptTemplate = new PromptTemplate("""
             你是一名医学知识问答助手，请优先基于给定资料回答，若资料不足请明确说明。
            
            【用户问题】
             {message}
            【参考资料】
              {context}
            """);

        return aiGatewayService.streamChat(AiScene.NORMAL_CHAT,
            promptTemplate.create(Map.of("message", message, "context", context)));
    }
}
