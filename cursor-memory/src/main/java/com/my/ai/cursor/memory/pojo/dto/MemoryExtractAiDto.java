package com.my.ai.cursor.memory.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * MemoryExtractAiDto
 *
 * @author 刘强
 * @version 2026/04/21 11:33
 **/
@Data
public class MemoryExtractAiDto {

    private String summary;

    private BigDecimal importance;

    private BigDecimal confidence;
}
