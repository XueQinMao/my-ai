package com.my.ai.cursor.ai.platform.application.pojo;

import com.my.ai.cursor.ai.platform.application.context.AgentContext;
import com.my.ai.cursor.ai.platform.application.context.RequestContext;
import com.my.ai.cursor.ai.platform.application.observability.AiMetricsRecorder;
import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentToolCallHandleDto;
import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentToolTraceDto;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class AgentRunTracker {

    private final ThreadLocal<RunState> runStateHolder = new ThreadLocal<>();

    public void startRun(AgentContext context) {
        runStateHolder.set(new RunState(Objects.requireNonNull(context, "Agent context cannot be null")));
    }

    public AgentToolCallHandleDto beginToolCall(String toolName, String argumentsSummary) {
        RunState state = requireState();
        int nextStep = state.toolCalls + 1;
        if (nextStep > state.context.maxToolCallsPerRun()) {
            AiMetricsRecorder.recordAgentCheckpoint(state.context.request(), state.context.taskType(), "TOOL_BUDGET",
                "REJECTED", state.toolCalls,
                "toolName=%s, budgetLimit=%s".formatted(toolName, state.context.maxToolCallsPerRun()));
            throw new IllegalStateException("Agent tool call limit exceeded: " + state.context.maxToolCallsPerRun());
        }
        state.toolCalls = nextStep;
        Instant startedAt = Instant.now();
        String taskDescription = generateTaskDescription(toolName, argumentsSummary);
        AiMetricsRecorder.recordAgentStep(state.context.request(), state.context.taskType(), nextStep, "ACT", toolName,
            "STARTED", 0L, argumentsSummary, null, null, null);
        return new AgentToolCallHandleDto(nextStep, toolName, argumentsSummary, System.nanoTime(), startedAt, taskDescription);
    }

    public void recordSuccess(AgentToolCallHandleDto handle, String resultSummary) {
        RunState state = requireState();
        Instant completedAt = Instant.now();
        long durationMs = elapsedMs(handle.startedAtNanos());
        state.toolTraces.add(AgentToolTraceDto.of(
            handle.stepNo(), 
            handle.toolName(), 
            handle.taskDescription(),
            handle.argumentsSummary(),
            resultSummary, 
            "SUCCESS", 
            durationMs, 
            null,
            handle.startedAt(),
            completedAt
        ));
        AiMetricsRecorder.recordAgentStep(state.context.request(), state.context.taskType(), handle.stepNo(), "OBSERVE",
            handle.toolName(), "SUCCESS", durationMs, handle.argumentsSummary(), resultSummary, null, null);
    }

    public List<AgentToolTraceDto> toolTraces() {
        RunState state = runStateHolder.get();
        return state == null ? List.of() : List.copyOf(state.toolTraces);
    }

    public void recordFailure(AgentToolCallHandleDto handle, Throwable throwable) {
        RunState state = requireState();
        Instant completedAt = Instant.now();
        long durationMs = elapsedMs(handle.startedAtNanos());
        state.toolTraces.add(AgentToolTraceDto.of(
            handle.stepNo(),
            handle.toolName(),
            handle.taskDescription(),
            handle.argumentsSummary(), 
            null,
            "FAILED", 
            durationMs, 
            throwable.getMessage(),
            handle.startedAt(),
            completedAt
        ));
        AiMetricsRecorder.recordAgentStep(state.context.request(), state.context.taskType(), handle.stepNo(), "OBSERVE",
            handle.toolName(), "FAILED", durationMs, handle.argumentsSummary(), null, "TOOL_EXECUTION_FAILED",
            throwable.getMessage());
    }

    public AgentContext currentContext() {
        RunState state = runStateHolder.get();
        return state == null ? null : state.context;
    }

    public RequestContext currentRequestContext() {
        AgentContext context = currentContext();
        return context == null ? null : context.request();
    }

    public int currentToolCallCount() {
        RunState state = runStateHolder.get();
        return state == null ? 0 : state.toolCalls;
    }

    public void finishRun() {
        runStateHolder.remove();
    }

    private RunState requireState() {
        RunState state = runStateHolder.get();
        if (state == null) {
            throw new IllegalStateException("No active agent run context");
        }
        return state;
    }

    private long elapsedMs(long startedAtNanos) {
        return (System.nanoTime() - startedAtNanos) / 1_000_000L;
    }

    private String generateTaskDescription(String toolName, String argumentsSummary) {
        return switch (toolName) {
            case "knowledgeSearch" -> "知识检索: " + (argumentsSummary != null ? argumentsSummary : "未知查询");
            case "memoryRecall" -> "记忆召回: " + (argumentsSummary != null ? argumentsSummary : "未知用户");
            case "chatHistory" -> "历史查询: " + (argumentsSummary != null ? argumentsSummary : "未知会话");
            default -> "工具调用: " + toolName;
        };
    }

    private static final class RunState {

        private final AgentContext context;

        private final List<AgentToolTraceDto> toolTraces = new ArrayList<>();

        private int toolCalls;

        private RunState(AgentContext context) {
            this.context = context;
        }
    }
}
