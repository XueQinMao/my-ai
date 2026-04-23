package com.my.ai.cursor.tool.support;

import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentExecutionRecorderDto;
import com.my.ai.cursor.ai.platform.application.pojo.context.AgentExecutionContext;
import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentToolCallHandleDto;
import com.my.ai.cursor.tool.model.dto.ToolMetadata;
import com.my.ai.cursor.tool.model.dto.ToolResult;

import java.util.function.Supplier;

public abstract class AbstractAgentTool {

    private final AgentExecutionRecorderDto agentExecutionRecorderDto;

    protected AbstractAgentTool(AgentExecutionRecorderDto agentExecutionRecorderDto) {
        this.agentExecutionRecorderDto = agentExecutionRecorderDto;
    }

    protected AgentExecutionContext currentContext() {
        return agentExecutionRecorderDto.currentContext();
    }

    protected ToolMetadata readonlyMetadata(String scope) {
        return new ToolMetadata(true, false, scope);
    }

    protected <T> ToolResult<T> executeReadonlyTool(String toolName, String argumentsSummary, String scope,
                                                    Supplier<T> supplier) {
        ToolMetadata metadata = readonlyMetadata(scope);
        AgentToolCallHandleDto handle = null;
        if (currentContext() != null) {
            // 通过统一包装把“开始计时、记录参数摘要、记录成功/失败”这类横切逻辑收口到父类。
            handle = agentExecutionRecorderDto.beginToolCall(toolName, argumentsSummary);
        }
        try {
            T data = supplier.get();
            if (handle != null) {
                agentExecutionRecorderDto.recordSuccess(handle, summarize(data));
            }
            return ToolResult.success(toolName, data, metadata);
        } catch (Exception e) {
            if (handle != null) {
                agentExecutionRecorderDto.recordFailure(handle, e);
            }
            return ToolResult.failure(toolName, metadata, "TOOL_EXECUTION_FAILED", e.getMessage(), false);
        }
    }

    private String summarize(Object value) {
        if (value == null) {
            return "null";
        }
        // 轨迹里只存一段简短摘要，避免把过长工具结果直接塞进日志。
        String text = String.valueOf(value);
        return text.length() <= 240 ? text : text.substring(0, 240);
    }
}
