package com.my.ai.cursor.ai.platform.application.agent.aspect;

import com.my.ai.cursor.ai.platform.application.context.AgentContext;
import com.my.ai.cursor.ai.platform.application.observability.AiMetricsRecorder;
import com.my.ai.cursor.ai.platform.application.pojo.AgentRunTracker;
import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentToolTraceDto;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.Duration;
import java.util.List;

@Aspect
@Component
public class AgentExecutionAspect {

    private static final Logger log = LoggerFactory.getLogger(AgentExecutionAspect.class);

    private final AgentRunTracker agentRunTracker;

    public AgentExecutionAspect(AgentRunTracker agentRunTracker) {
        this.agentRunTracker = agentRunTracker;
    }

    @Around("execution(* com.my.ai.cursor.ai.platform.application.agent.AgentExecutor.execute(..))")
    public Object observeAgentExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        AgentContext context = (AgentContext) joinPoint.getArgs()[0];
        Instant startedAt = context.request().startedAt();
        String runId = context.runId();

        AiMetricsRecorder.recordAgentRun(context.request(), context.taskType(), "EXECUTE", "RUNNING", startedAt, 0, 0, null, null);
        log.info("Agent run started. runId={}, userId={}, sessionId={}, scene={}",
            runId, context.userId(), context.sessionId(), context.scene());

        try {
            Object result = joinPoint.proceed();

            List<AgentToolTraceDto> traces = agentRunTracker.toolTraces();
            int totalTasks = traces.size();
            long successCount = traces.stream().filter(t -> "SUCCESS".equals(t.status())).count();
            long failedCount = traces.stream().filter(t -> "FAILED".equals(t.status())).count();

            AiMetricsRecorder.recordAgentRun(context.request(), context.taskType(), "FINISH", "COMPLETED",
                startedAt, agentRunTracker.currentToolCallCount(), agentRunTracker.currentToolCallCount(), null, null);

            log.info("Agent run completed. runId={}, status=COMPLETED, totalTasks={}, success={}, failed={}, duration={}ms",
                runId, totalTasks, successCount, failedCount, Duration.between(startedAt, Instant.now()).toMillis());

            if (totalTasks > 0) {
                logTaskSummary(runId, traces);
            }

            return result;
        } catch (Exception e) {
            List<AgentToolTraceDto> traces = agentRunTracker.toolTraces();
            AiMetricsRecorder.recordAgentRun(context.request(), context.taskType(), "FINISH", "FAILED", startedAt,
                agentRunTracker.currentToolCallCount(), agentRunTracker.currentToolCallCount(),
                "AGENT_EXECUTION_FAILED", e.getMessage());
            log.error("Agent run failed. runId={}, status=FAILED, totalTasks={}, executedBeforeFailure={}, error={}",
                runId, traces.size(), traces.size(), e.getMessage());
            throw e;
        }
    }

    private void logTaskSummary(String runId, List<AgentToolTraceDto> traces) {
        log.info("Agent task execution summary. runId={}, totalTasks={}", runId, traces.size());
        for (AgentToolTraceDto trace : traces) {
            String statusIcon = "SUCCESS".equals(trace.status()) ? "✓" : "✗";
            log.info("  [{}] Step {}: {} | Status: {} | Duration: {}ms | Error: {}",
                statusIcon,
                trace.stepNo(),
                trace.taskDescription() != null ? trace.taskDescription() : trace.toolName(),
                trace.status(),
                trace.durationMs(),
                trace.errorMessage() != null ? trace.errorMessage() : "none");
        }
    }
}
