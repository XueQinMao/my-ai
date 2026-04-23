package com.my.ai.cursor.ai.platform.application;

import com.my.ai.cursor.common.enums.AiScene;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class AiGatewayService {

    private final ChatClientRouter chatClientRouter;
    private final EmbeddingModelRouter embeddingModelRouter;
    private final VectorStoreRouter vectorStoreRouter;

    private final QuestionAnswerAdvisor qaAdvisor;

    public AiGatewayService(ChatClientRouter chatClientRouter, EmbeddingModelRouter embeddingModelRouter,
        VectorStoreRouter vectorStoreRouter, QuestionAnswerAdvisor qaAdvisor) {
        this.chatClientRouter = chatClientRouter;
        this.embeddingModelRouter = embeddingModelRouter;
        this.vectorStoreRouter = vectorStoreRouter;
        this.qaAdvisor = qaAdvisor;
    }

    public String chat(AiScene scene, String userMessage) {
        return chatClientRouter.route(scene).prompt().user(userMessage).call().content();
    }

    public String chat(AiScene scene, Prompt prompt) {
        return chatClientRouter.route(scene).prompt(prompt).call().content();
    }

    public String chat(AiScene scene, Prompt prompt, List<Object> tools) {
        return chatClientRouter.route(scene).prompt(prompt).tools(tools.toArray()).call().content();
    }

    public <T> T chat(AiScene scene, Prompt prompt, Class<T> _class) {
        return chatClientRouter.route(scene).prompt(prompt).call().entity(_class);
    }

    public Flux<String> streamChat(AiScene scene, String userMessage) {
        return chatClientRouter.route(scene).prompt().user(userMessage).stream().content();
    }

    public Flux<String> streamChat(AiScene scene, Prompt prompt) {
        return chatClientRouter.route(scene).prompt(prompt).stream().content();
    }

    public EmbeddingModel embeddingModel() {
        return embeddingModelRouter.route();
    }

    public void addDocuments(List<Document> documents) {
        vectorStoreRouter.route().add(documents);
    }

    public List<Document> similaritySearch(SearchRequest searchRequest) {
        return vectorStoreRouter.route().similaritySearch(searchRequest);
    }
}
