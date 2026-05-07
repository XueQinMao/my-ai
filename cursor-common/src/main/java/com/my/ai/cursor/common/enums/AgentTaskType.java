package com.my.ai.cursor.common.enums;

public enum AgentTaskType {
    FACT_ANSWER,
    PERSONALIZED_ANSWER,
    MEMORY_MAINTENANCE,
    KNOWLEDGE_CURATION,
    GENERAL_ASSISTANCE;

    public static AgentTaskType defaultForScene(AiScene scene) {
        if (scene == null) {
            return GENERAL_ASSISTANCE;
        }
        return switch (scene) {
            case AGENT_CHAT -> FACT_ANSWER;
            case EVALUATION_CHAT -> GENERAL_ASSISTANCE;
            case MEMORY_EXTRACTION -> MEMORY_MAINTENANCE;
            case RAG_CLEANING -> KNOWLEDGE_CURATION;
            default -> GENERAL_ASSISTANCE;
        };
    }
}
