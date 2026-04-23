package com.my.ai.cursor.tool.support;

import com.alibaba.fastjson.JSON;
import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentExecutionRecorderDto;
import com.my.ai.cursor.ai.platform.application.pojo.context.AgentExecutionContext;
import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentToolCallHandleDto;
import com.my.ai.cursor.tool.model.dto.ToolMetadata;
import com.my.ai.cursor.tool.model.dto.ToolResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Supplier;

public abstract class AbstractAgentTool {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final AgentExecutionRecorderDto agentExecutionRecorderDto;

    private static final int MAX_SUMMARY_LENGTH = 240;

    protected AbstractAgentTool(AgentExecutionRecorderDto agentExecutionRecorderDto) {
        this.agentExecutionRecorderDto = agentExecutionRecorderDto;
    }

    protected AgentExecutionContext currentContext() {
        return agentExecutionRecorderDto.currentContext();
    }

    protected ToolMetadata readonlyMetadata(String scope) {
        return new ToolMetadata(true, false, scope);
    }

    protected <T> ToolResult<T> executeReadonlyTool(String toolName, Map<String, Object>argumentsSummary, String scope,
        Supplier<T> supplier) {
        ToolMetadata metadata = readonlyMetadata(scope);
        AgentToolCallHandleDto handle =
            currentContext() != null ? agentExecutionRecorderDto.beginToolCall(toolName, null == argumentsSummary?"": JSON.toJSONString(argumentsSummary)) : null;
        T result;
        try {
             result = supplier.get();
            if (handle != null) {
                agentExecutionRecorderDto.recordSuccess(handle, summarize(result));
            }
            return ToolResult.success(toolName, result, metadata);
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
        String text = String.valueOf(value);
        return text.length() > MAX_SUMMARY_LENGTH ? text.substring(0, MAX_SUMMARY_LENGTH) : text;
    }
}
