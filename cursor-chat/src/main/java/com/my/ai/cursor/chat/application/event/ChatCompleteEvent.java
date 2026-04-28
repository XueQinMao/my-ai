package com.my.ai.cursor.chat.application.event;

import com.lmax.disruptor.EventFactory;
import com.my.ai.cursor.ai.platform.application.context.RequestContext;
import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentRunResult;
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

    private String assistant;

    private ChatRequest request;

    private RequestContext requestContext;

    private AgentRunResult agentRunResult;

    public static final EventFactory<ChatCompleteEvent> FACTORY = ChatCompleteEvent::new;

}
