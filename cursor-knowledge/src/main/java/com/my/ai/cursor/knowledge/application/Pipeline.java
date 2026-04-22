package com.my.ai.cursor.knowledge.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Pipeline <T, R>
 *
 * @author 刘强
 * @version 2026/04/13 16:57
 **/
public class Pipeline<T, R> {

    private static final Logger log = LoggerFactory.getLogger(Pipeline.class);

    private final List<PipelineStep<T, ?>> steps;

    private Pipeline(List<PipelineStep<T, ?>> steps) {
        this.steps = steps;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public R execute(T input) {
        Object currentResult = input;

        for (PipelineStep<?, ?> step : steps) {
            Instant start = Instant.now();
            @SuppressWarnings("unchecked")
            PipelineStep<Object, Object> typedStep = (PipelineStep<Object, Object>) step;
            currentResult = typedStep.execute(currentResult);
            log.info("Pipeline step {} executed in {} ms", step.getClass().getSimpleName(), ChronoUnit.MILLIS.between(start, Instant.now()));
        }

        @SuppressWarnings("unchecked")
        R finalResult = (R) currentResult;
        return finalResult;
    }

    public static class Builder<T> {
        private final List<PipelineStep<T, ?>> steps = new ArrayList<>();

        public Builder<T> addStep(List<? extends PipelineStep<T, ?>> steps) {
            this.steps.addAll(steps);
            return this;
        }

        public <R> Pipeline<T, R> build() {
            return new Pipeline<>(new ArrayList<>(steps));
        }
    }
}
