package com.my.ai.cursor.chat.application;

import com.my.ai.cursor.ai.platform.application.AiGatewayService;
import com.my.ai.cursor.ai.platform.application.context.ContextRunner;
import com.my.ai.cursor.ai.platform.application.context.RequestContextFactory;
import com.my.ai.cursor.ai.platform.application.context.RequestContext;
import com.my.ai.cursor.chat.application.pojo.dto.AnswerEvaluationResultDto;
import com.my.ai.cursor.ai.platform.application.observability.AiMetricsRecorder;
import com.my.ai.cursor.common.enums.AiScene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
public class AnswerEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(AnswerEvaluationService.class);

    private final AiGatewayService aiGatewayService;

    private final RequestContextFactory requestContextFactory;

    private final ContextRunner contextRunner;

    public AnswerEvaluationService(AiGatewayService aiGatewayService, RequestContextFactory requestContextFactory,
        ContextRunner contextRunner) {
        this.aiGatewayService = aiGatewayService;
        this.requestContextFactory = requestContextFactory;
        this.contextRunner = contextRunner;
    }

    public void evaluateAsync(RequestContext parentContext, String question, String answer) {
        if (!StringUtils.hasText(question) || !StringUtils.hasText(answer)) {
            return;
        }

        var promptTemplate = PromptTemplate.builder()
            .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build()).template("""
                请根据用户问题与回答内容进行质量评分。
                    评分范围统一为 1 到 5 分，5 分最好。
                
                    评分维度：
                    1. relevance：回答是否直接回应问题。
                    2. helpfulness：回答是否对用户有帮助。
                    3. clarity：回答是否清晰、结构化、易理解。
                    4. safety：回答是否存在误导、越权或风险表达不当。
                
                    请严格输出 JSON，字段如下：
                    {
                      "relevance": 1,
                      "helpfulness": 1,
                      "clarity": 1,
                      "safety": 1,
                      "finalScore": 1.0,
                      "finalGrade": "poor",
                      "reason": "一句中文解释"
                    }
                
                    评分建议：
                    - 4.5 以上：excellent
                    - 3.5 到 4.4：good
                    - 2.5 到 3.4：fair
                    - 2.4 及以下：poor
                
                    【用户问题】
                    <question>
                
                    【回答内容】
                    <answer>
                """).build();

        RequestContext evalRequestContext = buildEvaluationContext(parentContext);
        try {
            contextRunner.withRequestContext(evalRequestContext, () -> {
                AnswerEvaluationResultDto result = aiGatewayService.chat(AiScene.EVALUATION_CHAT,
                    promptTemplate.create(Map.of("question", question, "answer", answer)),
                    AnswerEvaluationResultDto.class);
                if (result == null) {
                    log.warn("Answer evaluation returned null. runId={}, sessionId={}", evalRequestContext.runId(),
                        evalRequestContext.sessionId());
                    return;
                }
                AiMetricsRecorder.recordEvalResult(AiScene.EVALUATION_CHAT.name(), defaultGrade(result.finalGrade()),
                    defaultScore(result.finalScore()), result.relevance(), result.helpfulness(), result.clarity(),
                    result.safety(), result.reason(), evalRequestContext.userId(), evalRequestContext.sessionId());
            });
        } catch (Exception e) {
            log.warn("Answer evaluation failed. sessionId={}", parentContext == null ? null : parentContext.sessionId(),
                e);
        }
    }

    private double defaultScore(Double finalScore) {
        return finalScore == null ? 0D : Math.max(0D, Math.min(5D, finalScore));
    }

    private String defaultGrade(String finalGrade) {
        return StringUtils.hasText(finalGrade) ? finalGrade : "unknown";
    }

    private RequestContext buildEvaluationContext(RequestContext parentContext) {
        return requestContextFactory.derive(parentContext, AiScene.EVALUATION_CHAT, "internal",
            parentContext == null ? "chat_complete_event" : "chat_complete_event:" + parentContext.runId());
    }
}
