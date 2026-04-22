package com.my.ai.cursor.common.enums;

public enum AiScene {
    NORMAL_CHAT,
    REASONING_CHAT,
    RAG_CLEANING,
    MEMORY_EXTRACTION;

    public static AiScene from(String scene) {
        if (scene == null || scene.isBlank()) {
            throw new IllegalArgumentException("AI scene cannot be blank");
        }
        for (AiScene value : values()) {
            if (value.name().equalsIgnoreCase(scene)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unsupported AI scene: " + scene);
    }
}
