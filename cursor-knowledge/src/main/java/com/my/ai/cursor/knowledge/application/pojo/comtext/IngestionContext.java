package com.my.ai.cursor.knowledge.application.pojo.comtext;

import com.my.ai.cursor.knowledge.application.pojo.dto.PreparedChunkDto;
import com.my.ai.cursor.knowledge.application.pojo.req.IngestRequest;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class IngestionContext {

    private final IngestRequest request;

    private byte[] pdfBytes;

    private int pageCount;

    private String extractedText;

    private String cleanedText;

    private String checksum;

    private String title;

    private Long duplicatedDocumentId;

    private List<PreparedChunkDto> chunks = new ArrayList<>();

    public IngestionContext(IngestRequest request) {
        this.request = request;
    }

    public boolean isDuplicated() {
        return duplicatedDocumentId != null;
    }
}
