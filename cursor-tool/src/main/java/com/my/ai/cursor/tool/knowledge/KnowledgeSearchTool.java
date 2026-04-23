package com.my.ai.cursor.tool.knowledge;

import com.my.ai.cursor.ai.platform.application.pojo.dto.AgentExecutionRecorderDto;
import com.my.ai.cursor.common.annotation.AgentToolGroup;
import com.my.ai.cursor.knowledge.application.KnowledgeSearchService;
import com.my.ai.cursor.knowledge.application.pojo.req.KnowledgeSearchRequest;
import com.my.ai.cursor.tool.model.dto.ToolResult;
import com.my.ai.cursor.tool.support.AbstractAgentTool;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Component
@AgentToolGroup("knowledge")
public class KnowledgeSearchTool extends AbstractAgentTool {

    private final KnowledgeSearchService knowledgeSearchService;

    public KnowledgeSearchTool(AgentExecutionRecorderDto agentExecutionRecorderDto,
        KnowledgeSearchService knowledgeSearchService) {
        super(agentExecutionRecorderDto);
        this.knowledgeSearchService = knowledgeSearchService;
    }

    @Tool(description = "根据用户问题从知识库检索相关事实、规范、文档片段和出处")
    public ToolResult<List<KnowledgeToolHit>> searchKnowledge(
        @ToolParam(description = "用户当前要检索的问题") String query,
        @ToolParam(description = "返回结果数量，建议 1 到 5") Integer topK) {
        return executeReadonlyTool("searchKnowledge", Map.of("topK",topK, "query",query), "knowledge", () -> {
                if (!StringUtils.hasText(query)) {
                    throw new IllegalArgumentException("query cannot be empty");
                }
                // 限制 topK 范围，避免模型一次拉取过多片段，把 prompt 和日志都撑大。
                int normalizedTopK = topK <= 0 ? 3 : Math.min(topK, 5);
                return knowledgeSearchService.search(new KnowledgeSearchRequest(query, normalizedTopK, null)).stream()
                    // Tool 层只暴露对模型友好的字段，不把业务实体和底层向量信息直接泄露出去。
                    .map(hit -> new KnowledgeToolHit(hit.title(), hit.sourceUrl(), hit.sourceOrg(), hit.docType(),
                        hit.content(), hit.score()))
                    .toList();
            });
    }
}
