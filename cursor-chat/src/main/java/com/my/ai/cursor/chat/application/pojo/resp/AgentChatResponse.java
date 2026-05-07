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
    int totalTasks,
    long successTaskCount,
    long failedTaskCount,
    List<AgentToolTraceDto> toolTraces,
    String errorMessage
) {

    public static AgentChatResponse of(String runId, String userId, String sessionId, AgentRunStatus status, 
                                       String content, List<AgentToolTraceDto> toolTraces, String errorMessage) {
        long successCount = toolTraces == null ? 0 : toolTraces.stream()
            .filter(t -> "SUCCESS".equals(t.status()))
            .count();
        long failedCount = toolTraces == null ? 0 : toolTraces.stream()
            .filter(t -> "FAILED".equals(t.status()))
            .count();
        return new AgentChatResponse(runId, userId, sessionId, status, content, 
                                     toolTraces != null ? toolTraces.size() : 0,
                                     successCount, failedCount,
                                     toolTraces, errorMessage);
    }
}
