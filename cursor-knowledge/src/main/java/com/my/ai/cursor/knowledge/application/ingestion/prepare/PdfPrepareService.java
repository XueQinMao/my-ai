package com.my.ai.cursor.knowledge.application.ingestion.prepare;


import com.my.ai.cursor.knowledge.application.Pipeline;
import com.my.ai.cursor.knowledge.application.PipelineStep;
import com.my.ai.cursor.knowledge.application.ingestion.pipeline.CheckProcessing;
import com.my.ai.cursor.knowledge.application.ingestion.pipeline.ContentCleanProcessing;
import com.my.ai.cursor.knowledge.application.ingestion.pipeline.ContentExtractionProcessing;
import com.my.ai.cursor.knowledge.application.ingestion.pipeline.ConvertProcessing;
import com.my.ai.cursor.knowledge.application.pojo.comtext.IngestionContext;
import com.my.ai.cursor.knowledge.application.pojo.req.IngestRequest;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class PdfPrepareService implements IngestionPreparer {

    private final Pipeline<IngestionContext, IngestionContext> pipeline;

    public PdfPrepareService(ContentExtractionProcessing contentExtractionProcessing,
                             ContentCleanProcessing contentCleanProcessing,
                             CheckProcessing checkProcessing,
                             ConvertProcessing convertProcessing) {
        List<? extends PipelineStep<IngestionContext, ?>> steps = Stream.of(
                contentExtractionProcessing,
                contentCleanProcessing,
                checkProcessing,
                convertProcessing
            )
            .sorted(Comparator.comparingInt(PipelineStep::order))
            .toList();
        this.pipeline = Pipeline.<IngestionContext>builder().addStep(steps).build();
    }

    @Override
    public boolean support(String type) {
        return Objects.equals(type, "PDF");
    }

    @Override
    public IngestionContext prepare(IngestRequest request) {
        return pipeline.execute(new IngestionContext(request));
    }
}
