package com.my.ai.cursor.ai.platform.application;

import com.my.ai.cursor.common.enums.AiScene;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class ChatClientRouter {

    private final Map<AiScene, ChatClient> chatClients;

    public ChatClientRouter(@Qualifier("normalChatClient") ChatClient normalChatClient,
        @Qualifier("reasoningChatClient") ChatClient reasoningChatClient,
        @Qualifier("ragCleaningChatClient") ChatClient ragCleaningChatClient) {
        this.chatClients = Map.ofEntries(Map.entry(AiScene.AGENT_CHAT, reasoningChatClient),
            Map.entry(AiScene.NORMAL_CHAT, normalChatClient), Map.entry(AiScene.RAG_CLEANING, ragCleaningChatClient),
            Map.entry(AiScene.MEMORY_EXTRACTION, ragCleaningChatClient));
    }

    public ChatClient route(AiScene scene) {
        ChatClient chatClient = chatClients.get(scene);
        if (chatClient == null) {
            throw new IllegalArgumentException("No ChatClient configured for scene: " + scene);
        }
        return chatClient;
    }
}
