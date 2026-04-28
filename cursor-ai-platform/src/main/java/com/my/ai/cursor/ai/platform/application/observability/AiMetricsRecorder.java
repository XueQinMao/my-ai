package com.my.ai.cursor.ai.platform.application.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class AiMetricsRecorder {

    private static final Logger log = LoggerFactory.getLogger(AiMetricsRecorder.class);

    /**
     * agent记录
     * @param scene
     * @param status
     * @param startedAt
     * @param toolCallCount
     */
    public static void recordAgentRun(String scene, String status, Instant startedAt, int toolCallCount) {

        logMetric("ai.agent.run", "scene", scene, "status", status, "durationMs", ChronoUnit.MILLIS.between(startedAt, Instant.now()), "toolCallCount",
            toolCallCount);
    }

    /**
     * llm调用记录
     * @param scene
     * @param model
     * @param status
     * @param streaming
     * @param durationMs
     * @param promptTokens
     * @param completionTokens
     * @param totalTokens
     * @param costAmount
     */
    public static void recordLlmCall(String scene, String model, String status, boolean streaming, long durationMs,
        Integer promptTokens, Integer completionTokens, Integer totalTokens, double costAmount) {
        logMetric("ai.llm.call", "scene", scene, "model", model, "status", status, "streaming", streaming,
            "durationMs", durationMs, "promptTokens", normalize(promptTokens), "completionTokens",
            normalize(completionTokens), "totalTokens", normalize(totalTokens), "costAmount",
            Math.max(costAmount, 0D));
    }

    /**
     * 工具调用记录
     * @param scene
     * @param toolName
     * @param status
     * @param durationMs
     */
    public static void recordToolCall(String scene, String toolName, String status, long durationMs) {
        logMetric("ai.tool.call", "scene", scene, "toolName", toolName, "status", status, "durationMs", durationMs);
    }

    /**
     * rag检索记录
     * @param scene
     * @param status
     * @param durationMs
     * @param hitCount
     */
    public static void recordRagRetrieve(String scene, String status, long durationMs, int hitCount) {
        logMetric("ai.rag.retrieve", "scene", scene, "status", status, "durationMs", durationMs, "hitCount",
            Math.max(hitCount, 0), "emptyHits", hitCount == 0);
    }

    /**
     * 评估日志。
     * 最小可用 Evaluation 仍然需要额外调用一次 judge 模型，这里只负责把评分结果结构化打印出来。
     */
    public static void recordEvalResult(String judgeModel, String finalGrade, double finalScore, Integer relevance,
        Integer helpfulness, Integer clarity, Integer safety, String reason, String userId, String sessionId) {
        logMetric("ai.eval.result", "judgeModel", judgeModel, "grade", finalGrade, "finalScore", finalScore,
            "relevance", normalize(relevance), "helpfulness", normalize(helpfulness), "clarity", normalize(clarity),
            "safety", normalize(safety), "reason", reason, "userId", userId, "sessionId", sessionId);
    }

    private static void logMetric(String metricName, Object... pairs) {
        // 这里故意使用稳定的 key=value 形式，方便后续被 ELK/Loki 等日志系统用正则或 pipeline 提取字段。
        StringBuilder builder = new StringBuilder("ai.metric name=").append(metricName);
        for (int i = 0; i < pairs.length; i += 2) {
            builder.append(", ").append(pairs[i]).append('=').append(sanitize(pairs[i + 1]));
        }
        log.info(builder.toString());
    }

    private static String sanitize(Object value) {
        if (value == null) {
            return "unknown";
        }
        String text = String.valueOf(value);
        return text.isBlank() ? "unknown" : text;
    }

    private static double normalize(Integer value) {
        return value == null || value < 0 ? 0D : value.doubleValue();
    }
}
