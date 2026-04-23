package com.my.ai.cursor.tool.memory;

import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentExecutionRecorderDto;
import com.my.ai.cursor.ai.platform.application.pojo.context.AgentExecutionContext;
import com.my.ai.cursor.common.annotation.AgentToolGroup;
import com.my.ai.cursor.memory.application.MemoryRecallService;
import com.my.ai.cursor.tool.model.dto.ToolResult;
import com.my.ai.cursor.tool.support.AbstractAgentTool;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Component
@AgentToolGroup("memory")
public class MemoryRecallTool extends AbstractAgentTool {

    private final MemoryRecallService memoryRecallService;

    public MemoryRecallTool(AgentExecutionRecorderDto agentExecutionRecorderDto, MemoryRecallService memoryRecallService) {
        super(agentExecutionRecorderDto);
        this.memoryRecallService = memoryRecallService;
    }

    @Tool(description = "检索当前用户的长期记忆，例如偏好、事实和过往稳定要求")
    public ToolResult<List<MemoryToolHit>> recallUserMemory(
        @ToolParam(description = "需要回忆的主题或问题") String query,
        @ToolParam(description = "返回记忆数量，建议 1 到 5") Integer limit) {
        return executeReadonlyTool("recallUserMemory", Map.of("query",query, "limit",  limit), "memory", () -> {
                AgentExecutionContext context = currentContext();
                // userId 不再让模型自己传入，直接从当前 agent 上下文拿，避免跨用户读记忆。
                if (context == null || !StringUtils.hasText(context.userId())) {
                    throw new IllegalStateException("Current agent run does not contain userId");
                }
                int recallLimit = limit <= 0 ? 3 : Math.min(limit, 5);
                return memoryRecallService.recall(context.userId(), query, recallLimit).stream()
                    .map(item -> new MemoryToolHit(item.memoryType(), item.summary(), item.content(), item.importance(),
                        item.confidence()))
                    .toList();
            });
    }
}
