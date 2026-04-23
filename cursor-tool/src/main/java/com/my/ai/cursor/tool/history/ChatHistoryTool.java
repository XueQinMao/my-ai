package com.my.ai.cursor.tool.history;

import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentExecutionRecorderDto;
import com.my.ai.cursor.ai.platform.application.pojo.context.AgentExecutionContext;
import com.my.ai.cursor.common.annotation.AgentToolGroup;
import com.my.ai.cursor.common.port.ChatHistoryQueryPort;
import com.my.ai.cursor.tool.model.dto.ToolResult;
import com.my.ai.cursor.tool.support.AbstractAgentTool;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
@AgentToolGroup("history")
public class ChatHistoryTool extends AbstractAgentTool {

    private final ChatHistoryQueryPort chatHistoryQueryPort;

    public ChatHistoryTool(AgentExecutionRecorderDto agentExecutionRecorderDto, ChatHistoryQueryPort chatHistoryQueryPort) {
        super(agentExecutionRecorderDto);
        this.chatHistoryQueryPort = chatHistoryQueryPort;
    }

    @Tool(description = "获取当前会话最近几轮对话历史，适用于需要承接上下文的问题")
    public ToolResult<List<ChatHistoryToolItem>> getRecentHistory(
        @ToolParam(description = "最近历史条数，建议 1 到 10") Integer limit) {
        return executeReadonlyTool("getRecentHistory", "limit=" + limit, "history", () -> {
            AgentExecutionContext context = currentContext();
            // sessionId 同样从运行上下文读取，避免模型任意查询其他会话的聊天记录。
            if (context == null || !StringUtils.hasText(context.sessionId())) {
                throw new IllegalStateException("Current agent run does not contain sessionId");
            }
            int historyLimit = limit == null || limit <= 0 ? 6 : Math.min(limit, 10);
            return chatHistoryQueryPort.getRecentHistory(context.sessionId(), historyLimit).stream()
                .map(item -> new ChatHistoryToolItem(item.id(), item.role(), item.content(), item.createdAt()))
                .toList();
        });
    }
}
