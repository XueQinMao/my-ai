package com.my.ai.cursor.tool.memory;

public record MemoryToolHit(String memoryType, String summary, String content, Double importance, Double confidence) {
}
