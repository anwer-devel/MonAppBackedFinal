package com.app.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResultDto {

    private Boolean isCorrect;

    private Integer pointsEarned;

    private Integer speedBonus;

    private String correctAnswer;

    private Integer currentScore;

    private CategoryContentResponseDto nextQuestion;
}
