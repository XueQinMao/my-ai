package com.my.ai.cursor.chat.application.event.handler;

import com.alibaba.fastjson.JSON;
import com.lmax.disruptor.EventHandler;
import com.my.ai.cursor.chat.application.ChatMessageService;
import com.my.ai.cursor.chat.application.event.ChatCompleteEvent;
import com.my.ai.cursor.chat.application.pojo.req.ChatRequest;
import com.my.ai.cursor.memory.application.LongTermMemoryService;
import com.my.ai.cursor.memory.application.ShortTermMemoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * ChatCompleteEventHandler
 *
 * @author 刘强
 * @version 2026/04/20 17:06
 **/
@Component
public class ChatCompleteEventHandler implements EventHandler<ChatCompleteEvent> {

    private static final Logger log = LoggerFactory.getLogger(ChatCompleteEventHandler.class);

    private final ChatMessageService chatMessageService;

    private final ShortTermMemoryService shortTermMemoryService;

    private final LongTermMemoryService longTermMemoryService;

    public ChatCompleteEventHandler(ChatMessageService chatMessageService,
        ShortTermMemoryService shortTermMemoryService, LongTermMemoryService longTermMemoryService) {
        this.chatMessageService = chatMessageService;
        this.shortTermMemoryService = shortTermMemoryService;
        this.longTermMemoryService = longTermMemoryService;
    }

    @Override
    public void onEvent(ChatCompleteEvent chatCompleteEvent, long sequence, boolean endOfBatch) throws Exception {
        log.info("ChatCompleteEvent:{} 位置:{} 是否结尾:{}", JSON.toJSONString(chatCompleteEvent), sequence,
            endOfBatch);
        //创建chat-session
        chatMessageService.checkOrCreateChatSession(chatCompleteEvent.getRequest());
        //保存用户的消息chat-message
        Long userMessageId = chatMessageService.saveUserMessage(chatCompleteEvent.getRequest());

        //保存模型输出的消息chat-message
        chatMessageService.saveAssistantMessage(chatCompleteEvent.getRequest(),
            chatCompleteEvent.getAssistantMessage());
        //短期的用户消息+模型消息记录
        persistShortTermMemory(chatCompleteEvent.getRequest(), chatCompleteEvent.getAssistantMessage());
        //触发长期记忆
        triggerLongTermMemory(chatCompleteEvent.getRequest(), userMessageId, chatCompleteEvent.getAssistantMessage());
    }

    private void persistShortTermMemory(ChatRequest request, String assistantMessage) {
        // Spring AI 的短期记忆只保存窗口消息，不承担完整聊天历史审计。
        shortTermMemoryService.addUserMessage(request.sessionId(), request.message());
        shortTermMemoryService.addAssistantMessage(request.sessionId(), assistantMessage);
    }

    private void triggerLongTermMemory(ChatRequest request, Long userMessageId, String assistantMessage) {
        if (!request.enableLongTermMemory()) {
            return;
        }
        // 长期记忆异步提炼，避免阻塞当前响应，尤其是 SSE 首 token。
        longTermMemoryService.extractAndStore(request.userId(), request.sessionId(), userMessageId, request.message(),
            assistantMessage);
    }

}
