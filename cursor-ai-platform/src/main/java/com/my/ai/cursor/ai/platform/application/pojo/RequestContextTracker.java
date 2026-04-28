package com.my.ai.cursor.ai.platform.application.pojo;

import com.my.ai.cursor.ai.platform.application.context.RequestContext;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * RequestContextTracker 仅负责同步调用链上的请求级上下文。
 */
@Component
public class RequestContextTracker {

    private final ThreadLocal<Deque<RequestContext>> requestContextHolder = ThreadLocal.withInitial(ArrayDeque::new);

    public void push(RequestContext context) {
        requestContextHolder.get().push(context);
    }

    public RequestContext currentContext() {
        Deque<RequestContext> stack = requestContextHolder.get();
        return stack.isEmpty() ? null : stack.peek();
    }

    public void pop() {
        Deque<RequestContext> stack = requestContextHolder.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
        if (stack.isEmpty()) {
            requestContextHolder.remove();
        }
    }
}
