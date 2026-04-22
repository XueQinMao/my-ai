package com.my.ai.cursor.ai.platform.application.pojo.dto;

import com.my.ai.cursor.common.enums.AiScene;

public record ResolvedChatDto(
    String userId,
    String sessionId,
    String message,
    AiScene scene,
    boolean enableKnowledge,
    boolean enableLongTermMemory,
    Integer memoryWindow
) {
    public String sceneName() {
        return scene.name();
    }
}
