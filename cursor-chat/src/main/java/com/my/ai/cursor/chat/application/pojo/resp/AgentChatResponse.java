package com.my.ai.cursor.chat.application.pojo.resp;

import com.my.ai.cursor.ai.platform.application.agent.AgentRunStatus;
import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentToolTraceDto;

import java.util.List;

public record AgentChatResponse(
    String runId,
    String userId,
    String sessionId,
    AgentRunStatus status,
    String content,
    List<AgentToolTraceDto> toolTraces,
    String errorMessage
) {

    public static AgentChatResponse of(String runId, String userId, String sessionId, AgentRunStatus status, String content, List<AgentToolTraceDto> toolTraces, String errorMessage) {
        return new AgentChatResponse(runId, userId, sessionId, status, content, toolTraces, errorMessage);
    }
}
