package com.my.ai.cursor.ai.platform.application.config;

import com.my.ai.cursor.ai.platform.application.VectorStoreRouter;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LlmAdvisorConfig
 *
 * @author 刘强
 * @version 2026/04/16 20:13
 **/
@Configuration
public class LlmAdvisorConfig {

    @Bean("qaAdvisor")
    public QuestionAnswerAdvisor questionAnswerAdvisor(VectorStoreRouter vectorStoreRouter) {
        var promptTemplate = PromptTemplate.builder()
            .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build()).template("""
                你是一名医学知识问答助手。请严格依据检索到的知识片段回答用户问题。
                        【用户问题】
                        <query>
                        【检索到的知识片段】
                        <question_answer_context>
                        【回答要求】
                        1. 仅依据知识片段作答，不要补充片段之外的事实或推断。
                        2. 若知识片段不足，请直接回答：根据当前知识库资料，暂时无法确定。
                        3. 不要出现“根据上下文”“根据提供的信息”等套话，直接回答问题。
                        4. 涉及诊断、治疗、用药、禁忌证、不良反应或风险判断时，如资料不足，必须明确说明无法确定。
                        5. 使用中文回答，内容准确、简洁、清晰；必要时可分点说明。
                """).build();
        return QuestionAnswerAdvisor.builder(vectorStoreRouter.route())
            .searchRequest(SearchRequest.builder().topK(3).similarityThreshold(0.9).build())
            .promptTemplate(promptTemplate).build();
    }
}
