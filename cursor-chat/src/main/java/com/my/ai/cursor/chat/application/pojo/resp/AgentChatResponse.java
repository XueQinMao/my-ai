package com.my.ai.cursor.chat.application.pojo.resp;

import com.my.ai.cursor.ai.platform.application.agent.AgentRunStatus;
import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentToolTraceDto;

import java.util.List;

public record AgentChatResponse(
    String runId,
    String userId,
    String sessionId,
    String scene,
    AgentRunStatus status,
    String content,
    int toolCallCount,
    List<AgentToolTraceDto> toolTraces,
    String errorMessage
) {
}
