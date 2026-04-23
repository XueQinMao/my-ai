package com.my.ai.cursor.memory.application;

import com.alibaba.fastjson2.JSON;
import com.my.ai.cursor.ai.platform.application.AiGatewayService;
import com.my.ai.cursor.common.enums.AiScene;
import com.my.ai.cursor.common.enums.MemoryStatus;
import com.my.ai.cursor.common.enums.MemoryType;
import com.my.ai.cursor.common.utils.DigestUtil;
import com.my.ai.cursor.memory.infrastructure.entity.AgentMemory;
import com.my.ai.cursor.memory.pojo.dto.ChatMemoryWriteCommand;
import com.my.ai.cursor.memory.pojo.dto.MemoryExtractAiDto;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class MemoryExtractionService {

    private final AiGatewayService aiGatewayService;

    public MemoryExtractionService(AiGatewayService aiGatewayService) {
        this.aiGatewayService = aiGatewayService;
    }

    public List<AgentMemory> extract(ChatMemoryWriteCommand command, Set<String> normalizedKeySet) {

        var promptTemplate = PromptTemplate.builder()
            .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build()).template("""
                你是记忆提取专家。请分析以下对话，提取所有有价值的长期记忆。
                   【用户消息】
                   <userMessage>
                   【AI 回答】
                    <assistantMessage>
                   【提取要求】
                     1. 识别用户偏好 (PREFERENCE)：如语言喜好、回答风格、饮食禁忌、兴趣爱好等。
                     2. 识别关键事实 (FACT)：如用户的职业、年龄、家庭状况、健康指标等。
                     3. 生成本轮摘要 (SESSION_SUMMARY)：对本轮对话核心内容的简短概括。
                   【输出格式】
                     请严格返回 JSON格式，不要包含 Markdown 标记或其他文字,格式如下：
                     {
                         "memories": [
                           {
                             "type": "PREFERENCE",
                             "summary": "偏好中文与简洁风格",
                             "importance": 0.9,
                             "confidence": 0.95
                           }
                         ]
                       }
                   【评分标准】
                     1. importance (0.0-1.0): 偏好和关键事实通常较高 (0.7+)，普通闲聊较低。
                     2. confidence (0.0-1.0): 用户明确表达的给高分，推断的给低分。
                """).build();

        var aiDto = aiGatewayService.chat(AiScene.MEMORY_EXTRACTION, promptTemplate.create(
                Map.of("userMessage", command.userMessage(), "assistantMessage", command.assistantMessage())),
            MemoryExtractAiDto.class);

        return Optional.ofNullable(aiDto).map(MemoryExtractAiDto::getMemories).orElse(Collections.emptyList()).stream()
            .map(memorie -> buildSessionSummary(command, memorie))
            .filter(memory -> !normalizedKeySet.contains(memory.getNormalizedKey())).toList();
    }

    private AgentMemory buildSessionSummary(ChatMemoryWriteCommand command, MemoryExtractAiDto.Memorie memorie) {
        AgentMemory memory = baseMemory(command, Enum.valueOf(MemoryType.class, memorie.getType()),
            DigestUtil.sha256(memorie.getSummary()));
        memory.setContent("本轮用户问题：" + command.userMessage() + "；本轮回答摘要：" + memorie.getSummary());
        memory.setSummary(memorie.getSummary());
        memory.setImportance(memorie.getImportance());
        memory.setConfidence(memorie.getConfidence());
        memory.setMetadataJson(JSON.toJSONString(Map.of("type", memorie.getType())));
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
