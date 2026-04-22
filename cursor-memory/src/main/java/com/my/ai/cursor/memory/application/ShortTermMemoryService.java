package com.my.ai.cursor.memory.application;

import com.my.ai.cursor.memory.application.config.AppMemoryProperties;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShortTermMemoryService {

    private final ChatMemory chatMemory;

    private final AppMemoryProperties appMemoryProperties;

    public ShortTermMemoryService(ChatMemory chatMemory, AppMemoryProperties appMemoryProperties) {
        this.chatMemory = chatMemory;
        this.appMemoryProperties = appMemoryProperties;
    }

    /**
     *
     * @param conversationId
     * @param overrideLimit
     * @return
     */
    public String generateShortMemoryContext(String conversationId, Integer overrideLimit) {
        if (!appMemoryProperties.getShortTerm().isEnabled() || !StringUtils.hasText(conversationId)) {
            return null;
        }

        List<Message> messages = chatMemory.get(conversationId);
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        // ChatMemory 可能保留比当前请求更多的窗口消息，这里允许按请求临时裁剪。
        int limit = (overrideLimit == null || overrideLimit <= 0) ? appMemoryProperties.getShortTerm().getWindowSize()
            : overrideLimit;
        int fromIndex = Math.max(0, messages.size() - limit);
        List<Message> messagesData = messages.subList(fromIndex, messages.size());
        return formatShortTermMessages(messagesData);
    }


    public void addUserMessage(String conversationId, String content) {
        if (appMemoryProperties.getShortTerm().isEnabled() && StringUtils.hasText(conversationId)
            && StringUtils.hasText(content)) {
            chatMemory.add(conversationId, new UserMessage(content));
        }
    }

    public void addAssistantMessage(String conversationId, String content) {
        if (appMemoryProperties.getShortTerm().isEnabled() && StringUtils.hasText(conversationId)
            && StringUtils.hasText(content)) {
            chatMemory.add(conversationId, new AssistantMessage(content));
        }
    }

    public void clear(String conversationId) {
        // 清理短期记忆只影响窗口上下文，不影响 chat_message 里的完整历史。
        if (StringUtils.hasText(conversationId)) {
            chatMemory.clear(conversationId);
        }
    }

    private String formatShortTermMessages(List<Message> messages) {
        return messages.stream().map(message -> "[%s] %s".formatted(message.getMessageType(), message.getText()))
            .collect(Collectors.joining("\n"));
    }
}
