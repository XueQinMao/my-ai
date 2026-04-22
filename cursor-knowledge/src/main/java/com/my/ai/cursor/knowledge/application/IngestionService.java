package com.my.ai.cursor.knowledge.application;


import com.my.ai.cursor.knowledge.application.ingestion.persistence.IngestionPersistenceService;
import com.my.ai.cursor.knowledge.application.ingestion.prepare.IngestionPreparer;
import com.my.ai.cursor.knowledge.application.pojo.comtext.IngestionContext;
import com.my.ai.cursor.knowledge.application.pojo.req.IngestRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class IngestionService {

    private final List<IngestionPreparer> preparers;

    private final IngestionPersistenceService ingestionPersistenceService;

    public IngestionService(List<IngestionPreparer> preparers,
                            IngestionPersistenceService ingestionPersistenceService) {
        this.preparers = preparers;
        this.ingestionPersistenceService = ingestionPersistenceService;
    }

    public void ingest(IngestRequest request) throws Exception {
        validate(request);
        var prepareService = preparers.stream().filter(service -> service.support(request.docType())).findFirst()
            .orElseThrow(() -> new RuntimeException("Unsupported docType: " + request.docType()));

        IngestionContext context = prepareService.prepare(request);
        if (context.isDuplicated()) {
            throw new RuntimeException("Document already exists, skip ingestion.");
        }

        ingestionPersistenceService.persist(context);
    }

    private void validate(IngestRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }
        if (!StringUtils.hasText(request.sourceUrl())) {
            throw new IllegalArgumentException("sourceUrl cannot be empty");
        }
        if (!request.sourceUrl().toLowerCase().contains(".pdf")) {
            throw new IllegalArgumentException("Only PDF sourceUrl is supported by this endpoint");
        }
    }

}
