package com.my.ai.cursor.chat.application.event;

import com.lmax.disruptor.EventFactory;
import com.my.ai.cursor.chat.application.pojo.req.ChatRequest;
import lombok.Data;

/**
 * ChatCompleteEvent
 *
 * @author 刘强
 * @version 2026/04/20 16:56
 **/
@Data
public class ChatCompleteEvent {

    private String assistantMessage;

    private ChatRequest request;

    public static final EventFactory<ChatCompleteEvent> FACTORY = ChatCompleteEvent::new;

}
