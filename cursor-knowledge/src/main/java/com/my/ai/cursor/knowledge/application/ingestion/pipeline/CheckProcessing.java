package com.my.ai.cursor.knowledge.application.ingestion.pipeline;

import com.my.ai.cursor.knowledge.application.PipelineStep;
import com.my.ai.cursor.knowledge.application.pojo.comtext.IngestionContext;
import com.my.ai.cursor.knowledge.domain.KnowledgeRepository;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * CheckProcessing
 *
 * @author 刘强
 * @version 2026/04/14 17:33
 **/
@Component
public class CheckProcessing implements PipelineStep<IngestionContext, IngestionContext> {

    private final KnowledgeRepository knowledgeRepository;

    public CheckProcessing(KnowledgeRepository knowledgeRepository) {
        this.knowledgeRepository = knowledgeRepository;
    }

    @Override
    public IngestionContext execute(IngestionContext input) {
        String checksum = sha256(input.getExtractedText());
        input.setChecksum(checksum);
        Optional<Long> documentIdByChecksum = knowledgeRepository.findDocumentIdByChecksum(checksum);
        if(documentIdByChecksum.isPresent()){
            throw new IllegalStateException("Duplicated document found, checksum=" + checksum);
        }
        return input;
    }

    @Override
    public int order() {
        return 1;
    }

    private String sha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }
}
