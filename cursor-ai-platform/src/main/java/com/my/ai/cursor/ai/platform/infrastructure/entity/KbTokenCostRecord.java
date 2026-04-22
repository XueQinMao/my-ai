package com.my.ai.cursor.ai.platform.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * KbTokenCostRecord
 *
 * @author 刘强
 * @version 2026/04/17 16:21
 **/
@Data
@TableName("kb_token_cost_record")
public class KbTokenCostRecord {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Long chatId;

    private String modelName;

    private Integer tokenCount;

    private Integer promptTokens;

    private Integer completionTokens;

    private LocalDateTime createdAt;

}
