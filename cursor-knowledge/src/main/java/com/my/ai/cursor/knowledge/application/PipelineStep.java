package com.my.ai.cursor.knowledge.application;

/**
 * PipelineStep
 *
 * @author 刘强
 * @version 2026/04/13 16:59
 **/
public interface PipelineStep<T, R> {
    R execute(T input);

    default int order() {
        return 999; // 默认优先级
    }
}
