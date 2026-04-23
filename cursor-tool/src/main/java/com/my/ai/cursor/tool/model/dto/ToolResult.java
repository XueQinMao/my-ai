package com.my.ai.cursor.tool.model.dto;

public record ToolResult<T>(
    String toolName,
    boolean success,
    T data,
    String errorCode,
    String errorMessage,
    boolean retryable,
    ToolMetadata metadata
) {
    public static <T> ToolResult<T> success(String toolName, T data, ToolMetadata metadata) {
        return new ToolResult<>(toolName, true, data, null, null, false, metadata);
    }

    public static <T> ToolResult<T> failure(String toolName, ToolMetadata metadata, String errorCode,
                                            String errorMessage, boolean retryable) {
        return new ToolResult<>(toolName, false, null, errorCode, errorMessage, retryable, metadata);
    }
}
