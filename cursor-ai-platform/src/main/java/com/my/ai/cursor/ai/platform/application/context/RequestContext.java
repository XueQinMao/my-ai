package com.my.ai.cursor.ai.platform.application.context;

import com.my.ai.cursor.common.enums.AiScene;

import java.time.Instant;
import java.util.UUID;

/**
 * RequestContext 请求级上下文，负责承载用户、会话和入口来源等公共身份信息。
 */
public record RequestContext(
    String requestId,
    String runId,
    String userId,
    String sessionId,
    AiScene scene,
    String channel,
    String source,
    Instant startedAt
) {

    public static RequestContext create(AiScene scene, String userId, String sessionId, String channel, String source) {
        return new RequestContext(UUID.randomUUID().toString(), UUID.randomUUID().toString(), userId, sessionId, scene,
            defaultText(channel, "internal"), defaultText(source, "unknown"), Instant.now());
    }

    public RequestContext derive(AiScene nextScene, String nextChannel, String nextSource) {
        return new RequestContext(UUID.randomUUID().toString(), UUID.randomUUID().toString(), userId, sessionId,
            nextScene, defaultText(nextChannel, channel), defaultText(nextSource, source), Instant.now());
    }

    private static String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
