package com.my.ai.cursor.ai.platform.application.context;

import com.my.ai.cursor.common.enums.AiScene;
import com.my.ai.cursor.common.utils.DigestUtil;

import java.time.Instant;
import java.util.UUID;

/**
 * LlmCallContext 单次 LLM 调用快照，不承载可变运行态。
 */
public record LlmCallContext(
    RequestContext request,
    String callId,
    String modelName,
    String bizAction,
    boolean streaming,
    String promptTemplateName,
    String promptHash,
    String promptPreview,
    Instant startedAt
) {

    private static final int MAX_PROMPT_PREVIEW_LENGTH = 400;

    public static LlmCallContext of(RequestContext request, String modelName, String bizAction, boolean streaming,
        String promptTemplateName, String promptText, Instant startedAt) {
        String normalizedPrompt = normalizePrompt(promptText);
        return new LlmCallContext(request, UUID.randomUUID().toString(), modelName, bizAction, streaming,
            promptTemplateName, normalizedPrompt == null ? null : DigestUtil.sha256(normalizedPrompt),
            abbreviate(normalizedPrompt), startedAt == null ? Instant.now() : startedAt);
    }

    public LlmCallContext withModelName(String resolvedModelName) {
        return new LlmCallContext(request, callId, resolvedModelName, bizAction, streaming, promptTemplateName,
            promptHash, promptPreview, startedAt);
    }

    public String runId() {
        return request == null ? null : request.runId();
    }

    public String userId() {
        return request == null ? null : request.userId();
    }

    public String sessionId() {
        return request == null ? null : request.sessionId();
    }

    public AiScene scene() {
        return request == null ? null : request.scene();
    }

    private static String normalizePrompt(String promptText) {
        if (promptText == null || promptText.isBlank()) {
            return null;
        }
        return promptText.trim();
    }

    private static String abbreviate(String promptText) {
        if (promptText == null) {
            return null;
        }
        return promptText.length() > MAX_PROMPT_PREVIEW_LENGTH ? promptText.substring(0, MAX_PROMPT_PREVIEW_LENGTH)
            : promptText;
    }
}
