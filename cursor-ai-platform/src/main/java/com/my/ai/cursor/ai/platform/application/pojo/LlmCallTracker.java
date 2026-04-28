package com.my.ai.cursor.ai.platform.application.pojo;

import com.my.ai.cursor.ai.platform.application.context.LlmCallContext;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * LlmCallTracker 使用栈结构保存当前线程内嵌套的 LLM 调用快照。
 */
@Component
public class LlmCallTracker {

    private final ThreadLocal<Deque<LlmCallContext>> contextStackHolder = ThreadLocal.withInitial(ArrayDeque::new);

    public void startCall(LlmCallContext context) {
        contextStackHolder.get().push(context);
    }

    public LlmCallContext currentContext() {
        Deque<LlmCallContext> stack = contextStackHolder.get();
        return stack.isEmpty() ? null : stack.peek();
    }

    public void finishCall() {
        Deque<LlmCallContext> stack = contextStackHolder.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
        if (stack.isEmpty()) {
            contextStackHolder.remove();
        }
    }
}
