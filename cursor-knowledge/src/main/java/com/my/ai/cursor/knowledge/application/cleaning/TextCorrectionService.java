package com.my.ai.cursor.knowledge.application.cleaning;

import com.my.ai.cursor.ai.platform.application.AiGatewayService;
import com.my.ai.cursor.common.enums.AiScene;
import com.my.ai.cursor.knowledge.application.pojo.dto.ChatCleanDto;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TextCorrectionService {

    private final AiGatewayService aiGatewayService;

    public TextCorrectionService(AiGatewayService aiGatewayService) {
        this.aiGatewayService = aiGatewayService;
    }

    public ChatCleanDto correct(String message, String domainName) {
        var promptTemplate = new PromptTemplate("""
            “你是一个{domainName}的文档校对专家，我需要你帮我纠正以下OCR识别后的文本内容中的错误。请注意，原始PDF文档可能存在文字识别错误（如错别字、漏字、多字）、格式错乱（如换行位置错误、标点符号缺失）或排版问题（如表格错位）。请仔细阅读以下文本，逐行检查并修正所有明显错误，同时尽量保持原文的格式和结构。如果需要确认不确定的内容，可以标记为[待确认]并给出可能的修正建议。
            输入文本（此处粘贴OCR后的文本内容）：
            {message}
            输出要求：
            1.修正所有识别错误，确保文本准确无误。
            2.调整格式问题，如恢复正确的段落分隔、标点符号位置等。
            3.若发现无法确定的错误，用[待确认：可能的修正选项]标注。
            4.保持与原始文档一致的排版结构（如标题、列表、表格等）。
            5.最终输出格式为清晰可读的纯文本（或根据需要指定Markdown/其他格式）。
            6.最多返回30个最重要的错误，避免输出过长。
            示例：
            ●原始OCR文本：'公司会汁报表显示，利润增长了20%。'
            ●正确修正：'公司会计报表显示，利润增长了20%。'（修正“会汁”为“会计”）
            ●请严格以JSON格式返回，不要包含任何其他文字说明，每个错误包含：
             - incorrect: 原始错误文本
             - correct: 修正后的文本
            """);

        return aiGatewayService.chat(
            AiScene.RAG_CLEANING,
            promptTemplate.create(Map.of("message", message, "domainName", domainName)),
            ChatCleanDto.class
        );
    }
}
