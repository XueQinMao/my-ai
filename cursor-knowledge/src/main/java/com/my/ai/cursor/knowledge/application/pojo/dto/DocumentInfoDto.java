package com.my.ai.cursor.knowledge.application.pojo.dto;

import lombok.Data;

/**
 * DocumentInfoDto
 *
 * @author 刘强
 * @version 2026/04/21 16:03
 **/
@Data
public class DocumentInfoDto {

    private Long chunkId;

    private Long documentId;

    private Integer chunkNo;

    private String title;

    private String sourceUrl;

    private String sourceOrg;

    private String docType;

    private String content;

    private String vectorDocId;
}
