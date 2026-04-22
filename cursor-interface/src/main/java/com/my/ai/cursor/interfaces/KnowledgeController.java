package com.my.ai.cursor.interfaces;

import com.my.ai.cursor.interfaces.pojo.vo.Response;
import com.my.ai.cursor.knowledge.application.IngestionService;
import com.my.ai.cursor.knowledge.application.KnowledgeSearchService;
import com.my.ai.cursor.knowledge.application.pojo.req.IngestRequest;
import com.my.ai.cursor.knowledge.application.pojo.req.KnowledgeSearchRequest;
import com.my.ai.cursor.knowledge.application.pojo.resp.KnowledgeSearchHit;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/kb")
public class KnowledgeController {

    private final IngestionService ingestionService;
    private final KnowledgeSearchService knowledgeSearchService;

    public KnowledgeController(IngestionService ingestionService,
                               KnowledgeSearchService knowledgeSearchService) {
        this.ingestionService = ingestionService;
        this.knowledgeSearchService = knowledgeSearchService;
    }

    @PostMapping("/ingest")
    public Response<String> ingest(@RequestBody IngestRequest request) {
        try {
            ingestionService.ingest(request);
            return Response.success(null);
        }catch (Exception e){
            return Response.error(500, e.getMessage());
        }
    }

    @PostMapping("/batchIngest")
    public Response<String> batchIngest(@RequestBody List<IngestRequest> request) {
        request.forEach(ingestRequest -> {
            try {
                ingestionService.ingest(ingestRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
       return Response.success(null);
    }


    @PostMapping("/search")
    public List<KnowledgeSearchHit> search(@RequestBody KnowledgeSearchRequest request) {
        return knowledgeSearchService.search(request);
    }
}
