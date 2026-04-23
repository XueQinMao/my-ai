package com.my.ai.cursor.tool.history;

import com.my.ai.cursor.ai.platform.application.pojo.context.AgentExecutionContext;
import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentExecutionRecorderDto;
import com.my.ai.cursor.chat.application.ChatMessageService;
import com.my.ai.cursor.chat.application.pojo.req.ChatHistoryQueryRequest;
import com.my.ai.cursor.chat.infrastructure.service.ChatSessionService;
import com.my.ai.cursor.common.annotation.AgentToolGroup;
import com.my.ai.cursor.tool.model.dto.ToolResult;
import com.my.ai.cursor.tool.support.AbstractAgentTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Component
@AgentToolGroup("history")
public class ChatHistoryTool extends AbstractAgentTool {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ChatMessageService chatMessageService;

    public ChatHistoryTool(AgentExecutionRecorderDto agentExecutionRecorderDto, ChatMessageService chatMessageService) {
        super(agentExecutionRecorderDto);
        this.chatMessageService = chatMessageService;
    }

    @Tool(description = """
        获取当前会话的对话历史记录。
                使用场景：
                - 当 Prompt 中的【最近对话窗口】不足以回答问题时
                - 需要查看更早之前的对话内容（超出最近几轮）
                - 需要精确查找某条特定的历史消息
                注意：如果当前上下文已经包含足够的历史信息，请勿调用此工具。
        """)
    public ToolResult<List<ChatHistoryToolItem>> getRecentHistory(
        @ToolParam(description = "最近历史条数，建议 1 到 10") Integer limit) {
        return executeReadonlyTool("getRecentHistory", Map.of("limit",  limit), "history", () -> {
            log.info("tool getRecentHistory limit {}", limit);
            AgentExecutionContext context = currentContext();
            // sessionId 同样从运行上下文读取，避免模型任意查询其他会话的聊天记录。
            if (context == null || !StringUtils.hasText(context.sessionId())) {
                throw new IllegalStateException("Current agent run does not contain sessionId");
            }
            int historyLimit = limit <= 0 ? 6 : Math.min(limit, 10);
            return chatMessageService.history(ChatHistoryQueryRequest.of(context.sessionId(), 1, historyLimit)).stream()
                .map(item -> new ChatHistoryToolItem(item.id(), item.role(), item.content(), item.createdAt()))
                .toList();
        });
    }
}
