package com.my.ai.cursor.common.enums;

public enum AiScene {
    AGENT_CHAT,//agent对话
    NORMAL_CHAT,//普通对话
    REASONING_CHAT,  //推理
    RAG_CLEANING, //RAG 清洗
    MEMORY_EXTRACTION,//记忆提取
    EVALUATION_CHAT;//质量分析

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
