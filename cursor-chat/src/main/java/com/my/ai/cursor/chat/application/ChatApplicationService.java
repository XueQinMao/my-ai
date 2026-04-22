package com.my.ai.cursor.chat.application;

import com.lmax.disruptor.dsl.Disruptor;
import com.my.ai.cursor.ai.platform.application.AiGatewayService;
import com.my.ai.cursor.memory.application.config.AppMemoryProperties;
import com.my.ai.cursor.chat.application.event.ChatCompleteEvent;
import com.my.ai.cursor.ai.platform.application.pojo.dto.ResolvedChatDto;
import com.my.ai.cursor.chat.application.pojo.req.ChatRequest;
import com.my.ai.cursor.chat.application.pojo.resp.ChatResponse;
import com.my.ai.cursor.knowledge.application.KnowledgeSearchService;
import com.my.ai.cursor.memory.application.LongTermMemoryService;
import com.my.ai.cursor.memory.application.ShortTermMemoryService;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.Map;

@Service
public class ChatApplicationService {

    private final AiGatewayService aiGatewayService;
    private final ChatOptionDecisionService chatOptionDecisionService;
    private final ShortTermMemoryService shortTermMemoryService;
    private final LongTermMemoryService longTermMemoryService;
    private final KnowledgeSearchService knowledgeSearchService;
    private final AppMemoryProperties appMemoryProperties;

    private final Disruptor<ChatCompleteEvent> chatCompleteDisruptor;

    public ChatApplicationService(AiGatewayService aiGatewayService,
        ChatOptionDecisionService chatOptionDecisionService, ShortTermMemoryService shortTermMemoryService,
        LongTermMemoryService longTermMemoryService, KnowledgeSearchService knowledgeSearchService,
        AppMemoryProperties appMemoryProperties, Disruptor<ChatCompleteEvent> chatCompleteDisruptor) {
        this.aiGatewayService = aiGatewayService;
        this.chatOptionDecisionService = chatOptionDecisionService;
        this.shortTermMemoryService = shortTermMemoryService;
        this.longTermMemoryService = longTermMemoryService;
        this.knowledgeSearchService = knowledgeSearchService;
        this.appMemoryProperties = appMemoryProperties;
        this.chatCompleteDisruptor = chatCompleteDisruptor;
    }

    public ChatResponse chat(ChatRequest request) {
        ResolvedChatDto resolvedRequest = chatOptionDecisionService.resolve(request);
        // Prompt 由三部分上下文拼装而成：短期记忆、长期记忆、可选知识库资料。
        Prompt prompt = generatePrompt(resolvedRequest);
        String assistantMessage = aiGatewayService.chat(resolvedRequest.scene(), prompt);

        //记忆系统先关
        chatCompleteDisruptor.getRingBuffer().publishEvent((event, sequence) -> {
            event.setAssistantMessage(assistantMessage);
            event.setRequest(resolvedRequest);
        });
        return buildResponse(resolvedRequest, assistantMessage);
    }

    public Flux<String> streamChat(ChatRequest request) {
        ResolvedChatDto resolvedRequest = chatOptionDecisionService.resolve(request);
        Prompt prompt = generatePrompt(resolvedRequest);
        StringBuilder assistantMessage = new StringBuilder();
        return aiGatewayService.streamChat(resolvedRequest.scene(), prompt).doOnNext(assistantMessage::append)
            .doOnComplete(() -> {
                // 流式输出结束后再补写助手消息，避免把半截响应写入业务消息表和记忆系统。
                String response = assistantMessage.toString();
                chatCompleteDisruptor.getRingBuffer().publishEvent((event, sequence) -> {
                    event.setAssistantMessage(response);
                    event.setRequest(resolvedRequest);
                });
            });
    }

    private Prompt generatePrompt(ResolvedChatDto request) {
        String historyContext = defaultContext(
            appMemoryProperties.getShortTerm().isEnabled() ? shortTermMemoryService.generateShortMemoryContext(
                request.sessionId(), request.memoryWindow()) : null);
        String longMemoryContext = defaultContext(
            request.enableLongTermMemory() ? longTermMemoryService.generateLongMemoryContext(request.userId(),
                 request.message(), appMemoryProperties.getLongTerm().getRecallLimit()) : null);
        String knowledgeContext = defaultContext(
            request.enableKnowledge() ? knowledgeSearchService.generateRagContext(request.message(), 3) : null);

        // 长期记忆强调“用户稳定偏好”，短期记忆强调“最近几轮上下文”，知识库负责外部事实补充。
        PromptTemplate promptTemplate = new PromptTemplate("""
            你是一个友好的 AI 助手，请用中文准确回答用户的问题。
            如果提供了长期记忆，请优先遵守其中关于用户偏好和风格的要求。
            如果提供了知识库参考资料，请仅在资料足够时引用，不足时明确说明资料不足。
            如果提供了长期记忆，请仅在记忆足够时引用，不足时明确说明无长期记忆。
            如果提供了最近对话，请仅在最近对话足够时引用，不足时明确说明无最近对话。
            
            【长期记忆】
            {memoryContext}
            
            【最近对话】
            {historyContext}
            
            【知识库参考资料】
            {knowledgeContext}
            
            【用户问题】
            {message}
            """);
        return promptTemplate.create(
            Map.of("memoryContext", longMemoryContext, "historyContext", historyContext, "knowledgeContext",
                knowledgeContext, "message", request.message()));
    }

    private ChatResponse buildResponse(ResolvedChatDto request, String content) {
        return new ChatResponse(request.userId(), request.sessionId(), request.sceneName(), content,
            request.enableKnowledge(), request.enableLongTermMemory());
    }

    private String defaultContext(String context) {
        return StringUtils.hasText(context) ? context : "无";
    }
}
