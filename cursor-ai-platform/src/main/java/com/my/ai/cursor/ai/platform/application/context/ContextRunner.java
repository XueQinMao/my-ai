package com.my.ai.cursor.ai.platform.application.context;

import com.my.ai.cursor.ai.platform.application.pojo.AgentRunTracker;
import com.my.ai.cursor.ai.platform.application.pojo.LlmCallTracker;
import com.my.ai.cursor.ai.platform.application.pojo.RequestContextTracker;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * ContextRunner 统一处理各类上下文的进入/退出逻辑，避免业务类直接调用 tracker。
 */
@Component
public class ContextRunner {

    private final RequestContextTracker requestContextTracker;
    private final AgentRunTracker agentRunTracker;
    private final LlmCallTracker llmCallTracker;

    public ContextRunner(RequestContextTracker requestContextTracker, AgentRunTracker agentRunTracker,
        LlmCallTracker llmCallTracker) {
        this.requestContextTracker = requestContextTracker;
        this.agentRunTracker = agentRunTracker;
        this.llmCallTracker = llmCallTracker;
    }

    public ContextScope openRequestContext(RequestContext context) {
        requestContextTracker.push(context);
        return requestContextTracker::pop;
    }

    public ContextScope openAgentContext(AgentContext context) {
        agentRunTracker.startRun(context);
        return agentRunTracker::finishRun;
    }

    public ContextScope openLlmCallContext(LlmCallContext context) {
        llmCallTracker.startCall(context);
        return llmCallTracker::finishCall;
    }

    public <T> T withRequestContext(RequestContext context, Supplier<T> action) {
        try (ContextScope ignored = openRequestContext(context)) {
            return action.get();
        }
    }

    public void withRequestContext(RequestContext context, Runnable action) {
        try (ContextScope ignored = openRequestContext(context)) {
            action.run();
        }
    }

    public <T> T withAgentContext(AgentContext context, Supplier<T> action) {
        try (ContextScope ignored = openAgentContext(context)) {
            return action.get();
        }
    }

    public void withAgentContext(AgentContext context, Runnable action) {
        try (ContextScope ignored = openAgentContext(context)) {
            action.run();
        }
    }

    public <T> T withLlmCallContext(LlmCallContext context, Supplier<T> action) {
        try (ContextScope ignored = openLlmCallContext(context)) {
            return action.get();
        }
    }

    public void withLlmCallContext(LlmCallContext context, Runnable action) {
        try (ContextScope ignored = openLlmCallContext(context)) {
            action.run();
        }
    }
}
