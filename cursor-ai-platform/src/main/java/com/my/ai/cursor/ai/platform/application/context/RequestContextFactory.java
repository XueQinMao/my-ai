package com.my.ai.cursor.ai.platform.application.context;

import com.my.ai.cursor.common.enums.AiScene;
import org.springframework.stereotype.Component;

/**
 * RequestContextFactory 负责请求级上下文的创建与派生。
 */
@Component
public class RequestContextFactory {

    public static RequestContext create(AiScene scene, String userId, String sessionId, String channel, String source) {
        return RequestContext.create(scene, userId, sessionId, channel, source);
    }

    public static RequestContext derive(RequestContext parentContext, AiScene nextScene, String nextChannel,
        String nextSource) {
        if (parentContext == null) {
            return create(nextScene, null, null, nextChannel, nextSource);
        }
        return parentContext.derive(nextScene, nextChannel, nextSource);
    }
}
