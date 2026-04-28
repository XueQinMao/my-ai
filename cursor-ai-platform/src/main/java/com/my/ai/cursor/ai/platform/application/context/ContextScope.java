package com.my.ai.cursor.ai.platform.application.context;

/**
 * ContextScope 用于统一管理上下文进入/退出生命周期。
 */
@FunctionalInterface
public interface ContextScope extends AutoCloseable {

    @Override
    void close();
}
