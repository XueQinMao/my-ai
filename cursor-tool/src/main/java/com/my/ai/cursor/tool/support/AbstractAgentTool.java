package com.my.ai.cursor.tool.support;

import com.alibaba.fastjson.JSON;
import com.my.ai.cursor.ai.platform.application.observability.AiMetricsRecorder;
import com.my.ai.cursor.ai.platform.application.pojo.AgentRunTracker;
import com.my.ai.cursor.ai.platform.application.context.AgentContext;
import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentToolCallHandleDto;
import com.my.ai.cursor.tool.model.dto.ToolMetadata;
import com.my.ai.cursor.tool.model.dto.ToolResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Supplier;

public abstract class AbstractAgentTool {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final AgentRunTracker agentRunTracker;

    private static final int MAX_SUMMARY_LENGTH = 240;

    protected AbstractAgentTool(AgentRunTracker agentRunTracker) {
        this.agentRunTracker = agentRunTracker;
    }

    protected AgentContext currentContext() {
        return agentRunTracker.currentContext();
    }

    protected ToolMetadata readonlyMetadata(String scope) {
        return new ToolMetadata(true, false, scope);
    }

    protected <T> ToolResult<T> executeReadonlyTool(String toolName, Map<String, Object>argumentsSummary, String scope,
        Supplier<T> supplier) {
        ToolMetadata metadata = readonlyMetadata(scope);
        AgentContext context = currentContext();
        int stepNo = context == null ? 0 : agentRunTracker.currentToolCallCount() + 1;
        if (context != null) {
            AiMetricsRecorder.recordToolDecision(context.request(), context.taskType(), stepNo, toolName, "EXECUTE",
                scope, metadata.readonly(), !metadata.requiresApproval(), agentRunTracker.currentToolCallCount(),
                context.maxToolCallsPerRun());
        }
        AgentToolCallHandleDto handle =
            context != null ? agentRunTracker.beginToolCall(toolName,
                null == argumentsSummary ? "" : JSON.toJSONString(argumentsSummary)) : null;
        long startedAtNanos = System.nanoTime();
        T result;
        try {
            result = supplier.get();
            long durationMs = elapsedMs(startedAtNanos);
            if (handle != null) {
                agentRunTracker.recordSuccess(handle, summarize(result));
            }
            if (handle != null) {
                AiMetricsRecorder.recordToolCall(context.request(), context.taskType(), handle.stepNo(), toolName,
                    "SUCCESS", durationMs);
            } else {
                AiMetricsRecorder.recordToolCall(context == null ? "UNKNOWN" : context.scene().name(), toolName,
                    "SUCCESS", durationMs);
            }
            return ToolResult.success(toolName, result, metadata);
        } catch (Exception e) {
            long durationMs = elapsedMs(startedAtNanos);
            if (handle != null) {
                agentRunTracker.recordFailure(handle, e);
            }
            if (handle != null && context != null) {
                AiMetricsRecorder.recordToolCall(context.request(), context.taskType(), handle.stepNo(), toolName,
                    "FAILED", durationMs);
            } else {
                AiMetricsRecorder.recordToolCall(context == null ? "UNKNOWN" : context.scene().name(), toolName,
                    "FAILED", durationMs);
            }
            log.warn("Tool execution failed. toolName={}, scope={}", toolName, scope, e);
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

    private long elapsedMs(long startedAtNanos) {
        return (System.nanoTime() - startedAtNanos) / 1_000_000L;
    }
}
