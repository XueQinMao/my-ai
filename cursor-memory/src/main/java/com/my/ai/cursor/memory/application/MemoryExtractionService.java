package com.my.ai.cursor.memory.application;

import com.alibaba.fastjson2.JSON;
import com.my.ai.cursor.ai.platform.application.AiGatewayService;
import com.my.ai.cursor.common.enums.AiScene;
import com.my.ai.cursor.common.enums.MemoryStatus;
import com.my.ai.cursor.common.enums.MemoryType;
import com.my.ai.cursor.memory.infrastructure.entity.AgentMemory;
import com.my.ai.cursor.memory.pojo.dto.ChatMemoryWriteCommand;
import com.my.ai.cursor.memory.pojo.dto.MemoryExtractAiDto;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class MemoryExtractionService {

    private final AiGatewayService aiGatewayService;

    public MemoryExtractionService(AiGatewayService aiGatewayService) {
        this.aiGatewayService = aiGatewayService;
    }

    public List<AgentMemory> extract(ChatMemoryWriteCommand command) {

        var promptTemplate = new PromptTemplate("""
            你是记忆提取专家。请从以下对话中提取关键记忆，并评估其质量。
                用户消息：{userMessage}
                AI 回答：{assistantMessage}
                请返回 JSON 格式,不要包含任何其他文字说明,包含如下内容：
                    - summary: 简短摘要
                    - importance: 0.85,   // 0.0-1.0，对用户未来对话的影响程度
                    - confidence: 0.92,  // 0.0-1.0，提取结果的准确确信度
                评分标准：
                - importance: 偏好(0.8-1.0), 关键事实(0.6-0.8), 普通信息(0.3-0.6)
                - confidence: 明确表达(0.9-1.0), 推断得出(0.6-0.9), 模糊不清(0.3-0.6)
            """);

        var aiDto = aiGatewayService.chat(AiScene.MEMORY_EXTRACTION, promptTemplate.create(
                Map.of("userMessage", command.userMessage(), "assistantMessage", command.assistantMessage())),
            MemoryExtractAiDto.class);

        return Stream.of(buildSessionSummary(command, aiDto), extractLanguagePreference(command),
            extractResponseStylePreference(command)).filter(Objects::nonNull).toList();
    }

    private AgentMemory buildSessionSummary(ChatMemoryWriteCommand command, MemoryExtractAiDto aiDto) {
        AgentMemory memory = baseMemory(command, MemoryType.SESSION_SUMMARY,
            "session-summary:" + command.sessionId() + ":" + command.sourceMessageId());
        memory.setContent("本轮用户问题：" + command.userMessage() + "；本轮回答摘要：" + aiDto.getSummary());
        memory.setSummary(aiDto.getSummary());
        memory.setImportance(aiDto.getImportance());
        memory.setConfidence(aiDto.getConfidence());
        memory.setMetadataJson(JSON.toJSONString(Map.of("source", "exchange-summary")));
        return memory;
    }

    private AgentMemory extractLanguagePreference(ChatMemoryWriteCommand command) {
        String userMessage = command.userMessage();
        if (!StringUtils.hasText(userMessage)) {
            return null;
        }
        // 这里刻意只提取“稳定偏好”，避免把临时表达误存成长期记忆。
        if (userMessage.contains("中文")) {
            AgentMemory memory = preferenceMemory(command, "preference:language", "用户偏好中文回答", "偏好中文");
            memory.setMetadataJson(JSON.toJSONString(Map.of("language", "zh-CN")));
            return memory;
        }
        if (userMessage.contains("英文")) {
            AgentMemory memory = preferenceMemory(command, "preference:language", "用户偏好英文回答", "偏好英文");
            memory.setMetadataJson(JSON.toJSONString(Map.of("language", "en-US")));
            return memory;
        }
        return null;
    }

    private AgentMemory extractResponseStylePreference(ChatMemoryWriteCommand command) {
        String userMessage = command.userMessage();
        if (!StringUtils.hasText(userMessage)) {
            return null;
        }
        if (userMessage.contains("简洁")) {
            AgentMemory memory = preferenceMemory(command, "preference:style", "用户偏好简洁回答", "偏好简洁风格");
            memory.setMetadataJson(JSON.toJSONString(Map.of("style", "concise")));
            return memory;
        }
        if (userMessage.contains("详细")) {
            AgentMemory memory = preferenceMemory(command, "preference:style", "用户偏好详细回答", "偏好详细风格");
            memory.setMetadataJson(JSON.toJSONString(Map.of("style", "detailed")));
            return memory;
        }
        return null;
    }

    private AgentMemory preferenceMemory(ChatMemoryWriteCommand command, String normalizedKey, String content,
                                         String summary) {
        AgentMemory memory = baseMemory(command, MemoryType.PREFERENCE, normalizedKey);
        memory.setContent(content);
        memory.setSummary(summary);
        memory.setImportance(BigDecimal.valueOf(0.80d));
        memory.setConfidence(BigDecimal.valueOf(0.90d));
        return memory;
    }

    private AgentMemory baseMemory(ChatMemoryWriteCommand command, MemoryType type, String normalizedKey) {
        LocalDateTime now = LocalDateTime.now();
        AgentMemory memory = new AgentMemory();
        memory.setUserId(command.userId());
        memory.setSessionId(command.sessionId());
        memory.setMemoryType(type.name());
        // normalizedKey 用于后续做去重、覆盖更新或向量化前的逻辑归并。
        memory.setNormalizedKey(normalizedKey);
        memory.setStatus(MemoryStatus.ACTIVE.name());
        memory.setVectorStoreId(UUID.randomUUID().toString());
        memory.setSourceMessageId(command.sourceMessageId());
        memory.setCreatedAt(now);
        memory.setUpdatedAt(now);
        return memory;
    }
}
