package com.my.ai.cursor.ai.platform.application.pojo;

import com.my.ai.cursor.ai.platform.application.context.AgentContext;
import com.my.ai.cursor.ai.platform.application.context.RequestContext;
import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentToolCallHandleDto;
import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentToolTraceDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class AgentRunTracker {

    // Agent 执行与工具调用发生在同一条请求线程上时，用 ThreadLocal 保存运行态最简单直接。
    private final ThreadLocal<RunState> runStateHolder = new ThreadLocal<>();

    public void startRun(AgentContext context) {
        runStateHolder.set(new RunState(Objects.requireNonNull(context, "Agent context cannot be null")));
    }

    public AgentToolCallHandleDto beginToolCall(String toolName, String argumentsSummary) {
        RunState state = requireState();
        int nextStep = state.toolCalls + 1;
        if (nextStep > state.context.maxToolCallsPerRun()) {
            throw new IllegalStateException("Agent tool call limit exceeded: " + state.context.maxToolCallsPerRun());
        }
        // 每次开始工具调用时，都会递增 stepNo，后续日志和返回结果可以按 step 回放。
        state.toolCalls = nextStep;
        return new AgentToolCallHandleDto(nextStep, toolName, argumentsSummary, System.nanoTime());
    }

    public void recordSuccess(AgentToolCallHandleDto handle, String resultSummary) {
        RunState state = requireState();
        state.toolTraces.add(new AgentToolTraceDto(handle.stepNo(), handle.toolName(), handle.argumentsSummary(),
            resultSummary, "SUCCESS", elapsedMs(handle.startedAtNanos()), null));
    }

    public List<AgentToolTraceDto> toolTraces() {
        RunState state = runStateHolder.get();
        // 返回不可变快照，避免外部在 run 结束前误改内部轨迹列表。
        return state == null ? List.of() : List.copyOf(state.toolTraces);
    }

    public void recordFailure(AgentToolCallHandleDto handle, Throwable throwable) {
        RunState state = requireState();
        state.toolTraces.add(new AgentToolTraceDto(handle.stepNo(), handle.toolName(), handle.argumentsSummary(), null,
            "FAILED", elapsedMs(handle.startedAtNanos()), throwable.getMessage()));
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

    private static final class RunState {

        private final AgentContext context;

        private final List<AgentToolTraceDto> toolTraces = new ArrayList<>();

        private int toolCalls;

        private RunState(AgentContext context) {
            this.context = context;
        }
    }
}
