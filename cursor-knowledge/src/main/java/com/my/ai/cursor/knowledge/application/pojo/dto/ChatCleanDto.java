package com.my.ai.cursor.knowledge.application.pojo.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * ChatCleanDto
 *
 * @author 刘强
 * @version 2026/04/13 19:24
 **/
@Data
public class ChatCleanDto {

    private List<Error> errors;;

    @Data
    public static class Error {
        private String incorrect;

        private String correct;

//        @JSONField(name = "error_type")
//        private String errorType;
//
//        private String domain;
//
//        private String suggestion;
    }

}
