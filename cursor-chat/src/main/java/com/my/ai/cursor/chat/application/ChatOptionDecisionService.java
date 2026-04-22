package com.my.ai.cursor.chat.application;

import com.my.ai.cursor.ai.platform.application.pojo.dto.ResolvedChatDto;
import com.my.ai.cursor.chat.application.pojo.req.ChatRequest;
import com.my.ai.cursor.common.enums.AiScene;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

/**
 * 决策实现 这个本应该使用知识库去实现的，但是为了简单起见，这里直接使用
 */
@Service
public class ChatOptionDecisionService {

    private static final List<String> REASONING_KEYWORDS = List.of(
        "为什么", "原因", "分析", "推导", "推理", "一步一步", "逐步", "拆解", "论证", "证明",
        "方案", "对比", "权衡", "取舍", "怎么设计", "如何设计", "复杂", "排查", "定位", "calculate",
        "reason", "think step by step");

    private static final List<String> KNOWLEDGE_KEYWORDS = List.of(
        "指南", "共识", "规范", "标准", "文档", "资料", "知识库", "出处", "依据", "药品", "药物",
        "诊断", "治疗", "用药", "禁忌", "不良反应", "副作用", "医学", "高血压", "糖尿病");

    private static final List<String> LONG_TERM_MEMORY_KEYWORDS = List.of(
        "之前", "刚才", "继续", "上次", "记得", "我的偏好", "我的习惯", "我喜欢", "以后都", "一直都",
        "还按", "延续", "保持之前", "按照我之前", "像以前那样");

    private static final List<String> TRANSFORMATION_KEYWORDS = List.of(
        "翻译", "润色", "改写", "重写", "扩写", "缩写", "总结", "提炼", "清洗", "纠错");

    public ResolvedChatDto resolve(ChatRequest request) {
        AiScene scene = resolveScene(request);
        boolean enableKnowledge = resolveKnowledge(request, scene);
        boolean enableLongTermMemory = resolveLongTermMemory(request, scene);
        return new ResolvedChatDto(request.userId(), request.sessionId(), request.message(), scene, enableKnowledge,
            enableLongTermMemory, request.memoryWindow());
    }

    private AiScene resolveScene(ChatRequest request) {
        if (StringUtils.hasText(request.scene())) {
            return AiScene.from(request.scene());
        }

        String message = normalize(request.message());
        if (!StringUtils.hasText(message)) {
            return AiScene.NORMAL_CHAT;
        }
        if (containsAny(message, TRANSFORMATION_KEYWORDS)) {
            return AiScene.NORMAL_CHAT;
        }
        if (containsAny(message, REASONING_KEYWORDS) || looksLikeMathOrLogicQuestion(message)) {
            return AiScene.REASONING_CHAT;
        }
        return AiScene.NORMAL_CHAT;
    }

    private boolean resolveKnowledge(ChatRequest request, AiScene scene) {
        if (request.enableKnowledge() != null) {
            return request.enableKnowledge();
        }

        String message = normalize(request.message());
        if (!StringUtils.hasText(message) || containsAny(message, TRANSFORMATION_KEYWORDS)) {
            return false;
        }
        if (scene == AiScene.RAG_CLEANING || scene == AiScene.MEMORY_EXTRACTION) {
            return false;
        }
        return containsAny(message, KNOWLEDGE_KEYWORDS);
    }

    private boolean resolveLongTermMemory(ChatRequest request, AiScene scene) {
        if (request.enableLongTermMemory() != null) {
            return request.enableLongTermMemory();
        }

        if (!StringUtils.hasText(request.userId())) {
            return false;
        }

        String message = normalize(request.message());
        if (!StringUtils.hasText(message) || scene == AiScene.RAG_CLEANING || scene == AiScene.MEMORY_EXTRACTION) {
            return false;
        }
        return containsAny(message, LONG_TERM_MEMORY_KEYWORDS);
    }

    private boolean looksLikeMathOrLogicQuestion(String message) {
        return message.contains("=") || message.contains("方程") || message.contains("概率") || message.contains("证明")
            || message.contains("如果") && message.contains("那么");
    }

    private boolean containsAny(String text, List<String> keywords) {
        return keywords.stream().anyMatch(text::contains);
    }

    private String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
