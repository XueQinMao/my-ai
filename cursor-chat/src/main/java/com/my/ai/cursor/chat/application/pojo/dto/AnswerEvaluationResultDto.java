package com.my.ai.cursor.chat.application.pojo.dto;

public record AnswerEvaluationResultDto(Integer relevance, Integer helpfulness, Integer clarity, Integer safety,
                                        Double finalScore, String finalGrade, String reason) {
}
