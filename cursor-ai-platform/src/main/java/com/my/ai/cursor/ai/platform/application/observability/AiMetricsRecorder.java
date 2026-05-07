package com.my.ai.cursor.ai.platform.application.observability;

import com.my.ai.cursor.ai.platform.application.context.RequestContext;
import com.my.ai.cursor.common.enums.AgentTaskType;
import com.my.ai.cursor.common.enums.AiScene;
import com.my.ai.cursor.common.utils.DigestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class AiMetricsRecorder {

    private static final Logger log = LoggerFactory.getLogger(AiMetricsRecorder.class);

    private static final int MAX_PREVIEW_LENGTH = 120;

    public static void recordAgentRun(String scene, String status, Instant startedAt, int toolCallCount) {
        logMetric("ai.agent.run", "scene", scene, "status", status, "durationMs",
            between(startedAt, Instant.now()), "toolCallCount", toolCallCount);
    }

    public static void recordAgentRun(RequestContext requestContext, AgentTaskType taskType, String eventType,
        String status, Instant startedAt, int toolCallCount, Integer stepCount, String errorCode, String errorMessage) {
        List<Object> pairs = standardPairs(requestContext, taskType);
        pairs.add("eventType");
        pairs.add(eventType);
        pairs.add("status");
        pairs.add(status);
        pairs.add("durationMs");
        pairs.add(between(startedAt, Instant.now()));
        pairs.add("toolCallCount");
        pairs.add(Math.max(toolCallCount, 0));
        pairs.add("stepCount");
        pairs.add(stepCount == null ? Math.max(toolCallCount, 0) : Math.max(stepCount, 0));
        pairs.add("errorCode");
        pairs.add(errorCode);
        pairs.add("errorMessage");
        pairs.add(abbreviate(errorMessage));
        logMetric("ai.agent.run.lifecycle", pairs);
    }

    public static void recordAgentPlan(RequestContext requestContext, AgentTaskType taskType, String planSummary,
        String initialQuestion, int maxSteps, int maxToolCallsPerRun, boolean enableKnowledge,
        boolean enableLongTermMemory, int memoryWindow) {
        List<Object> pairs = standardPairs(requestContext, taskType);
        pairs.add("eventType");
        pairs.add("PLAN");
        pairs.add("planSummary");
        pairs.add(abbreviate(planSummary));
        pairs.add("planHash");
        pairs.add(hash(planSummary));
        pairs.add("questionPreview");
        pairs.add(abbreviate(initialQuestion));
        pairs.add("questionHash");
        pairs.add(hash(initialQuestion));
        pairs.add("maxSteps");
        pairs.add(Math.max(maxSteps, 0));
        pairs.add("maxToolCallsPerRun");
        pairs.add(Math.max(maxToolCallsPerRun, 0));
        pairs.add("enableKnowledge");
        pairs.add(enableKnowledge);
        pairs.add("enableLongTermMemory");
        pairs.add(enableLongTermMemory);
        pairs.add("memoryWindow");
        pairs.add(Math.max(memoryWindow, 0));
        logMetric("ai.agent.plan", pairs);
    }

    public static void recordAgentStep(RequestContext requestContext, AgentTaskType taskType, int stepNo, String phase,
        String toolName, String status, long durationMs, String argumentsSummary, String resultSummary, String errorCode,
        String errorMessage) {
        List<Object> pairs = standardPairs(requestContext, taskType);
        pairs.add("eventType");
        pairs.add("STEP");
        pairs.add("stepNo");
        pairs.add(Math.max(stepNo, 0));
        pairs.add("phase");
        pairs.add(phase);
        pairs.add("toolName");
        pairs.add(toolName);
        pairs.add("status");
        pairs.add(status);
        pairs.add("durationMs");
        pairs.add(Math.max(durationMs, 0L));
        pairs.add("argumentsSummary");
        pairs.add(abbreviate(argumentsSummary));
        pairs.add("argumentsHash");
        pairs.add(hash(argumentsSummary));
        pairs.add("resultSummary");
        pairs.add(abbreviate(resultSummary));
        pairs.add("resultHash");
        pairs.add(hash(resultSummary));
        pairs.add("errorCode");
        pairs.add(errorCode);
        pairs.add("errorMessage");
        pairs.add(abbreviate(errorMessage));
        logMetric("ai.agent.step", pairs);
    }

    public static void recordAgentCheckpoint(RequestContext requestContext, AgentTaskType taskType, String checkpointType,
        String status, int stepNo, String payloadSummary) {
        List<Object> pairs = standardPairs(requestContext, taskType);
        pairs.add("eventType");
        pairs.add("CHECKPOINT");
        pairs.add("checkpointType");
        pairs.add(checkpointType);
        pairs.add("status");
        pairs.add(status);
        pairs.add("stepNo");
        pairs.add(Math.max(stepNo, 0));
        pairs.add("payloadSummary");
        pairs.add(abbreviate(payloadSummary));
        pairs.add("payloadHash");
        pairs.add(hash(payloadSummary));
        logMetric("ai.agent.checkpoint", pairs);
    }

    public static void recordToolDecision(RequestContext requestContext, AgentTaskType taskType, int stepNo,
        String toolName, String decision, String scope, boolean readonly, boolean idempotent, int budgetUsed,
        int budgetLimit) {
        List<Object> pairs = standardPairs(requestContext, taskType);
        pairs.add("eventType");
        pairs.add("TOOL_DECISION");
        pairs.add("stepNo");
        pairs.add(Math.max(stepNo, 0));
        pairs.add("toolName");
        pairs.add(toolName);
        pairs.add("decision");
        pairs.add(decision);
        pairs.add("scope");
        pairs.add(scope);
        pairs.add("readonly");
        pairs.add(readonly);
        pairs.add("idempotent");
        pairs.add(idempotent);
        pairs.add("budgetUsed");
        pairs.add(Math.max(budgetUsed, 0));
        pairs.add("budgetLimit");
        pairs.add(Math.max(budgetLimit, 0));
        pairs.add("budgetRemaining");
        pairs.add(Math.max(budgetLimit - budgetUsed, 0));
        logMetric("ai.agent.tool.decision", pairs);
    }

    public static void recordLlmCall(String scene, String model, String status, boolean streaming, long durationMs,
        Integer promptTokens, Integer completionTokens, Integer totalTokens, double costAmount) {
        logMetric("ai.llm.call", "scene", scene, "model", model, "status", status, "streaming", streaming,
            "durationMs", durationMs, "promptTokens", normalize(promptTokens), "completionTokens",
            normalize(completionTokens), "totalTokens", normalize(totalTokens), "costAmount",
            Math.max(costAmount, 0D));
    }

    public static void recordLlmCall(RequestContext requestContext, AgentTaskType taskType, String model, String status,
        boolean streaming, long durationMs, Integer promptTokens, Integer completionTokens, Integer totalTokens,
        double costAmount) {
        List<Object> pairs = standardPairs(requestContext, taskType);
        pairs.add("model");
        pairs.add(model);
        pairs.add("status");
        pairs.add(status);
        pairs.add("streaming");
        pairs.add(streaming);
        pairs.add("durationMs");
        pairs.add(Math.max(durationMs, 0L));
        pairs.add("promptTokens");
        pairs.add(normalize(promptTokens));
        pairs.add("completionTokens");
        pairs.add(normalize(completionTokens));
        pairs.add("totalTokens");
        pairs.add(normalize(totalTokens));
        pairs.add("costAmount");
        pairs.add(Math.max(costAmount, 0D));
        logMetric("ai.llm.call", pairs);
    }

    public static void recordToolCall(String scene, String toolName, String status, long durationMs) {
        logMetric("ai.tool.call", "scene", scene, "toolName", toolName, "status", status, "durationMs", durationMs);
    }

    public static void recordToolCall(RequestContext requestContext, AgentTaskType taskType, int stepNo, String toolName,
        String status, long durationMs) {
        List<Object> pairs = standardPairs(requestContext, taskType);
        pairs.add("stepNo");
        pairs.add(Math.max(stepNo, 0));
        pairs.add("toolName");
        pairs.add(toolName);
        pairs.add("status");
        pairs.add(status);
        pairs.add("durationMs");
        pairs.add(Math.max(durationMs, 0L));
        logMetric("ai.tool.call", pairs);
    }

    public static void recordRagRetrieve(String scene, String status, long durationMs, int hitCount) {
        logMetric("ai.rag.retrieve", "scene", scene, "status", status, "durationMs", durationMs, "hitCount",
            Math.max(hitCount, 0), "emptyHits", hitCount == 0);
    }

    public static void recordKnowledgeAction(String scene, String userId, String sessionId, AgentTaskType taskType,
        String action, String status, long durationMs, int hitCount, Double similarityThreshold, String queryText) {
        List<Object> pairs = standardPairs(null, taskType, scene, userId, sessionId);
        pairs.add("eventType");
        pairs.add(action);
        pairs.add("status");
        pairs.add(status);
        pairs.add("durationMs");
        pairs.add(Math.max(durationMs, 0L));
        pairs.add("hitCount");
        pairs.add(Math.max(hitCount, 0));
        pairs.add("emptyHits");
        pairs.add(hitCount == 0);
        pairs.add("similarityThreshold");
        pairs.add(similarityThreshold);
        pairs.add("queryPreview");
        pairs.add(abbreviate(queryText));
        pairs.add("queryHash");
        pairs.add(hash(queryText));
        logMetric("ai.agent.knowledge.action", pairs);
    }

    public static void recordEvalResult(String judgeModel, String finalGrade, double finalScore, Integer relevance,
        Integer helpfulness, Integer clarity, Integer safety, String reason, String userId, String sessionId) {
        List<Object> pairs = standardPairs(null, AgentTaskType.GENERAL_ASSISTANCE, AiScene.EVALUATION_CHAT.name(), userId,
            sessionId);
        pairs.add("judgeModel");
        pairs.add(judgeModel);
        pairs.add("grade");
        pairs.add(finalGrade);
        pairs.add("finalScore");
        pairs.add(finalScore);
        pairs.add("relevance");
        pairs.add(normalize(relevance));
        pairs.add("helpfulness");
        pairs.add(normalize(helpfulness));
        pairs.add("clarity");
        pairs.add(normalize(clarity));
        pairs.add("safety");
        pairs.add(normalize(safety));
        pairs.add("reason");
        pairs.add(abbreviate(reason));
        logMetric("ai.eval.result", pairs);
    }

    public static void recordEvalResult(RequestContext requestContext, AgentTaskType taskType, String judgeModel,
        String finalGrade, double finalScore, Integer relevance, Integer helpfulness, Integer clarity, Integer safety,
        String reason) {
        List<Object> pairs = standardPairs(requestContext, taskType);
        pairs.add("judgeModel");
        pairs.add(judgeModel);
        pairs.add("grade");
        pairs.add(finalGrade);
        pairs.add("finalScore");
        pairs.add(finalScore);
        pairs.add("relevance");
        pairs.add(normalize(relevance));
        pairs.add("helpfulness");
        pairs.add(normalize(helpfulness));
        pairs.add("clarity");
        pairs.add(normalize(clarity));
        pairs.add("safety");
        pairs.add(normalize(safety));
        pairs.add("reason");
        pairs.add(abbreviate(reason));
        logMetric("ai.eval.result", pairs);
    }

    public static void recordEvalCase(RequestContext requestContext, AgentTaskType taskType, String caseType,
        String finalGrade, double finalScore, String reason, boolean requiresReview) {
        List<Object> pairs = standardPairs(requestContext, taskType);
        pairs.add("eventType");
        pairs.add(caseType);
        pairs.add("grade");
        pairs.add(finalGrade);
        pairs.add("finalScore");
        pairs.add(finalScore);
        pairs.add("requiresReview");
        pairs.add(requiresReview);
        pairs.add("reason");
        pairs.add(abbreviate(reason));
        logMetric("ai.agent.eval.case", pairs);
    }

    public static void recordMemoryAction(String scene, String userId, String sessionId, AgentTaskType taskType,
        String action, String status, int affectedCount, String detail, String errorCode, String errorMessage) {
        List<Object> pairs = standardPairs(null, taskType, scene, userId, sessionId);
        pairs.add("eventType");
        pairs.add(action);
        pairs.add("status");
        pairs.add(status);
        pairs.add("affectedCount");
        pairs.add(Math.max(affectedCount, 0));
        pairs.add("detail");
        pairs.add(abbreviate(detail));
        pairs.add("detailHash");
        pairs.add(hash(detail));
        pairs.add("errorCode");
        pairs.add(errorCode);
        pairs.add("errorMessage");
        pairs.add(abbreviate(errorMessage));
        logMetric("ai.agent.memory.action", pairs);
    }

    public static void recordGovernance(RequestContext requestContext, AgentTaskType taskType, String riskLevel,
        boolean approvalRequired, String decision, String reason) {
        List<Object> pairs = standardPairs(requestContext, taskType);
        pairs.add("eventType");
        pairs.add("POLICY_DECISION");
        pairs.add("riskLevel");
        pairs.add(riskLevel);
        pairs.add("approvalRequired");
        pairs.add(approvalRequired);
        pairs.add("decision");
        pairs.add(decision);
        pairs.add("reason");
        pairs.add(abbreviate(reason));
        logMetric("ai.agent.governance", pairs);
    }

    private static void logMetric(String metricName, List<Object> pairs) {
        logMetric(metricName, pairs.toArray());
    }

    private static void logMetric(String metricName, Object... pairs) {
        // 这里故意使用稳定的 key=value 形式，方便后续被 ELK/Loki 等日志系统用正则或 pipeline 提取字段。
        StringBuilder builder = new StringBuilder("ai.metric name=").append(metricName);
        for (int i = 0; i < pairs.length; i += 2) {
            builder.append(", ").append(pairs[i]).append('=').append(sanitize(pairs[i + 1]));
        }
        log.info(builder.toString());
    }

    private static List<Object> standardPairs(RequestContext requestContext, AgentTaskType taskType) {
        String scene = requestContext == null || requestContext.scene() == null ? "UNKNOWN" : requestContext.scene().name();
        String userId = requestContext == null ? null : requestContext.userId();
        String sessionId = requestContext == null ? null : requestContext.sessionId();
        return standardPairs(requestContext, taskType, scene, userId, sessionId);
    }

    private static List<Object> standardPairs(RequestContext requestContext, AgentTaskType taskType, String scene,
        String userId, String sessionId) {
        List<Object> pairs = new ArrayList<>();
        pairs.add("requestId");
        pairs.add(requestContext == null ? null : requestContext.requestId());
        pairs.add("runId");
        pairs.add(requestContext == null ? null : requestContext.runId());
        pairs.add("userId");
        pairs.add(userId);
        pairs.add("sessionId");
        pairs.add(sessionId);
        pairs.add("scene");
        pairs.add(scene);
        pairs.add("channel");
        pairs.add(requestContext == null ? null : requestContext.channel());
        pairs.add("source");
        pairs.add(requestContext == null ? null : requestContext.source());
        pairs.add("taskType");
        pairs.add(resolveTaskType(taskType, scene).name());
        return pairs;
    }

    private static String sanitize(Object value) {
        if (value == null) {
            return "unknown";
        }
        String text = String.valueOf(value);
        return text.isBlank() ? "unknown" : text.replace('\n', ' ').replace('\r', ' ');
    }

    private static double normalize(Integer value) {
        return value == null || value < 0 ? 0D : value.doubleValue();
    }

    private static long between(Instant startedAt, Instant endAt) {
        if (startedAt == null || endAt == null) {
            return 0L;
        }
        return Math.max(ChronoUnit.MILLIS.between(startedAt, endAt), 0L);
    }

    private static String abbreviate(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String normalized = text.trim().replace('\n', ' ').replace('\r', ' ');
        return normalized.length() > MAX_PREVIEW_LENGTH ? normalized.substring(0, MAX_PREVIEW_LENGTH) : normalized;
    }

    private static String hash(String text) {
        return text == null || text.isBlank() ? null : DigestUtil.sha256(text.trim());
    }

    private static AgentTaskType resolveTaskType(AgentTaskType taskType, String scene) {
        if (taskType != null) {
            return taskType;
        }
        if (scene == null || scene.isBlank() || "UNKNOWN".equalsIgnoreCase(scene)) {
            return AgentTaskType.GENERAL_ASSISTANCE;
        }
        try {
            return AgentTaskType.defaultForScene(AiScene.from(scene));
        } catch (IllegalArgumentException ignored) {
            return AgentTaskType.GENERAL_ASSISTANCE;
        }
    }
}
