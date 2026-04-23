package com.my.ai.cursor.ai.platform.application.config;

import com.my.ai.cursor.ai.platform.application.advisors.LlmLogAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * ApiServiceConfig
 *
 * @author Liu Qiang
 * @version 2026/04/08 19:42
 **/
@Configuration
public class ApiServiceConfig {

    @Value("${spring.ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    @Value("${spring.ai.openai.base-url:https://dashscope.aliyuncs.com/compatible-mode}")
    private String openAiBaseUrl;


    @Bean
    public RestClient.Builder ollamaRestClientBuilder() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(30));
        requestFactory.setReadTimeout(Duration.ofMinutes(30));
        return RestClient.builder().requestFactory(requestFactory);
    }

//    @Bean("ragCleaningChatModel")
//    public OllamaChatModel ragCleaningChatModel(RestClient.Builder ollamaRestClientBuilder) {
//        OllamaApi ollamaApi = OllamaApi.builder()
//            .baseUrl(ollamaBaseUrl)
//            .restClientBuilder(ollamaRestClientBuilder)
//            .build();
//        return OllamaChatModel.builder()
//            .ollamaApi(ollamaApi)
//            .defaultOptions(
//                OllamaChatOptions.builder()
//                    .model("Lusizo/qwen2.5-7b-instruct-1m:latest")
//                    .temperature(0.1)
//                    .build())
//            .build();
//    }

    /**
     * 数据清理的chat-model
     * @param ollamaRestClientBuilder
     * @return
     */
    @Bean("ragCleaningChatModel")
    public OpenAiChatModel ragCleaningChatModel(RestClient.Builder ollamaRestClientBuilder) {
        OpenAiApi openAiApi = OpenAiApi.builder()
            .baseUrl(openAiBaseUrl)
            .apiKey(openAiApiKey)
            .restClientBuilder(ollamaRestClientBuilder)
            .build();
        return OpenAiChatModel.builder()
            .openAiApi(openAiApi)
            .defaultOptions(
                OpenAiChatOptions.builder()
                    .model("qwen2.5-7b-instruct-1m")
                    .temperature(0.1)
                    .build())
            .build();
    }

    /***
     * 问答的chat-model
     * @param ollamaRestClientBuilder
     * @return
     */
    @Bean("normalOpenAiChatModel")
    public OpenAiChatModel openAiChatModel(RestClient.Builder ollamaRestClientBuilder) {
        OpenAiApi openAiApi = OpenAiApi.builder()
            .baseUrl(openAiBaseUrl)
            .apiKey(openAiApiKey)
            .restClientBuilder(ollamaRestClientBuilder)
            .build();
        return OpenAiChatModel.builder()
            .openAiApi(openAiApi)
            .defaultOptions(
                OpenAiChatOptions.builder()
                    .model("qwen-max")
                    .temperature(0.7)
                    .maxTokens(2048)
                    .build())
            .build();
    }

    /**
     * 推理的chat-model
     * @param ollamaRestClientBuilder
     * @return
     */
    @Bean("agentChatModel")
    public OpenAiChatModel reasoningChatModel(RestClient.Builder ollamaRestClientBuilder) {

        OpenAiApi openAiApi =
            OpenAiApi.builder().baseUrl(openAiBaseUrl).apiKey(openAiApiKey).restClientBuilder(ollamaRestClientBuilder)
                .build();
        OpenAiChatOptions openAiChatOptions =
            OpenAiChatOptions.builder().model("qwq-plus").temperature(0.6).maxTokens(8192).topP(0.95).build();
        return OpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(openAiChatOptions).build();
    }

    @Bean("normalChatClient")
    public ChatClient normalChatClient(@Qualifier("normalOpenAiChatModel") OpenAiChatModel openAiChatModel, LlmLogAdvisor llmLogAdvisor) {
        return ChatClient.builder(openAiChatModel)
            .defaultSystem("你是一个友好的 AI 助手，请用中文准确回答用户的问题")
            .defaultAdvisors(llmLogAdvisor)
            .build();
    }

    @Bean("agentChatClient")
    public ChatClient agentChatClient(@Qualifier("agentChatModel") OpenAiChatModel openAiChatModel, LlmLogAdvisor llmLogAdvisor) {
        return ChatClient.builder(openAiChatModel)
            .defaultAdvisors(llmLogAdvisor)
            .defaultSystem("""
                你是一个具备工具调用能力的中文智能 Agent。
                在回答前请先判断是否需要调用工具获取事实、记忆或历史上下文。
                规则如下：
                1. 如果问题需要知识库依据，优先调用知识检索工具。
                2. 如果问题涉及用户偏好、长期事实或历史延续，优先调用长期记忆工具。
                3. 如果问题明显依赖最近对话上下文，调用历史对话工具。
                4. 只有当你已经获取到足够证据时，才输出最终答案。
                5. 若工具返回失败或信息不足，请明确说明限制，不要编造结果。
                6. 请使用中文回答。
                """)
            .build();
    }

    @Bean("ragCleaningChatClient")
    public ChatClient ragCleaningChatClient(@Qualifier("ragCleaningChatModel") OpenAiChatModel ragCleaningChatModel, LlmLogAdvisor llmLogAdvisor) {
        return ChatClient.builder(ragCleaningChatModel)
            .defaultAdvisors(llmLogAdvisor)
            .defaultSystem("""
              你是一位专业字符识别与语言规范专家，专注于识别文本中的特殊字符、易混淆字符及专业领域错误用字。
              请严格遵循以下规则：
              1. 仅输出 JSON 结果，不要输出额外说明。
              2. 识别全角半角混淆、形近字、音近字和专业术语误写。
              3. 标注错误字符、正确字符、Unicode 编码、错误类型、适用领域、示例和修改建议。
              4. 结合上下文判断，不要孤立分析单个字符。
              """).build();
    }

    @Bean("reasoningChatClient")
    public ChatClient reasoningChatClient(@Qualifier("normalOpenAiChatModel") OpenAiChatModel reasoningChatModel, LlmLogAdvisor llmLogAdvisor) {
        return ChatClient.builder(reasoningChatModel)
            .defaultAdvisors(llmLogAdvisor)
            .defaultSystem("""
            你是一个擅长逻辑推理和问题分析的 Agent。
            在回答前请先判断是否需要调用工具获取事实、记忆或历史上下文，请：
              1. 先分析问题本质和关键要素
              2. 逐步推导，展示思考过程
              3. 给出清晰的结论
              4. 如有不确定性，明确说明
              5. 如果问题需要知识库依据，优先调用知识检索工具。
              6. 如果问题涉及用户偏好、长期事实或历史延续，优先调用长期记忆工具。
              7. 如果问题明显依赖最近对话上下文，调用历史对话工具。
              8. 只有当你已经获取到足够证据时，才输出最终答案。
              9. 若工具返回失败或信息不足，请明确说明限制，不要编造结果。
              请用中文回答。
            """).build();
    }
}
