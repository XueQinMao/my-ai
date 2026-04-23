package com.my.ai.cursor.tool.model.dto;

public record ToolMetadata(
    boolean readonly,
    boolean requiresApproval,
    String scope
) {
}
