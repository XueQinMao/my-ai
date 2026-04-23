package com.my.ai.cursor.chat.application.pojo.req;

public record ChatHistoryQueryRequest(String sessionId, Integer pageNo, Integer pageSize) {

    public static ChatHistoryQueryRequest of(String sessionId, Integer pageNo, Integer pageSize){
        return new ChatHistoryQueryRequest("", pageNo, pageSize);
    }
}
