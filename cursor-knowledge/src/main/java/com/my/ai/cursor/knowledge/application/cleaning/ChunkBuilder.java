package com.my.ai.cursor.knowledge.application.cleaning;

import com.my.ai.cursor.knowledge.application.pojo.dto.PreparedChunkDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class ChunkBuilder {

    private static final int CHUNK_SIZE = 800;
    private static final int CHUNK_OVERLAP = 120;

    public List<PreparedChunkDto> build(String text) {
        String normalized = text == null ? "" : text.replace('\r', '\n').trim();
        if (normalized.isEmpty()) {
            return List.of();
        }

        List<PreparedChunkDto> chunks = new ArrayList<>();
        int start = 0;
        int length = normalized.length();
        int chunkNo = 1;
        while (start < length) {
            int end = Math.min(start + CHUNK_SIZE, length);
            String chunkText = normalized.substring(start, end).trim();
            if (!chunkText.isEmpty()) {
                chunks.add(new PreparedChunkDto(chunkNo++, chunkText, estimateTokenCount(chunkText), UUID.randomUUID().toString()));
            }
            if (end >= length) {
                break;
            }
            start = Math.max(0, end - CHUNK_OVERLAP);
        }
        return chunks;
    }

    private int estimateTokenCount(String text) {
        return Math.max(1, text.length() / 2);
    }
}
