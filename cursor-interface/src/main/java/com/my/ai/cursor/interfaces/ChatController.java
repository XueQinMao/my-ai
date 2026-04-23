package com.my.ai.cursor.interfaces;

import com.my.ai.cursor.chat.application.ChatApplicationService;
import com.my.ai.cursor.chat.application.ChatMessageService;
import com.my.ai.cursor.chat.application.pojo.dto.ChatMessageDto;
import com.my.ai.cursor.chat.application.pojo.req.ChatHistoryQueryRequest;
import com.my.ai.cursor.chat.application.pojo.req.ChatRequest;
import com.my.ai.cursor.chat.application.pojo.resp.AgentChatResponse;
import com.my.ai.cursor.chat.application.pojo.resp.ChatResponse;
import com.my.ai.cursor.interfaces.pojo.vo.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatApplicationService chatApplicationService;

    private final ChatMessageService chatMessageService;

    public ChatController(ChatApplicationService chatApplicationService, ChatMessageService chatMessageService) {
        this.chatApplicationService = chatApplicationService;
        this.chatMessageService = chatMessageService;
    }

    /**
     *
     * @param request
     * @return
     */
    @PostMapping("")
    public Response<AgentChatResponse> agentChat(@RequestBody ChatRequest request) {
        return Response.success(chatApplicationService.agentChat(request));
    }

    @GetMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> streamAgentChat(@RequestParam String message,
                                        @RequestParam(required = false) String userId,
                                        @RequestParam(required = false) String sessionId,
                                        @RequestParam(required = false) Boolean enableKnowledge,
                                        @RequestParam(required = false) Boolean enableLongTermMemory,
                                        @RequestParam(required = false) Integer memoryWindow) {
        return chatApplicationService.agentStreamChat(
            new ChatRequest(userId, sessionId, message, null, enableKnowledge, enableLongTermMemory, memoryWindow));
    }

    @GetMapping("/history")
    public Response<List<ChatMessageDto>> history(@RequestParam String sessionId,
                                                  @RequestParam(required = false, defaultValue = "1") Integer pageNo,
                                                  @RequestParam(required = false, defaultValue = "20") Integer pageSize) {
        return Response.success(chatMessageService.history(new ChatHistoryQueryRequest(sessionId, pageNo, pageSize)));
    }
}
