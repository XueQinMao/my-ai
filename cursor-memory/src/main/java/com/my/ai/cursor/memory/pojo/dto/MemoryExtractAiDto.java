package com.my.ai.cursor.memory.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * MemoryExtractAiDto
 *
 * @author 刘强
 * @version 2026/04/21 11:33
 **/
@Data
public class MemoryExtractAiDto {

    private List<Memorie> memories;

    @Data
    public static class Memorie {
        private String type;

        private String summary;

        private BigDecimal importance;

        private BigDecimal confidence;
    }
}
