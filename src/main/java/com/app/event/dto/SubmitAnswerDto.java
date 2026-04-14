package com.app.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAnswerDto {

    @NotNull(message = "Content ID is required")
    private UUID contentId;

    @NotNull(message = "Question index is required")
    private Integer questionIndex;

    @NotBlank(message = "Selected answer is required")
    private String selectedAnswer;

    private Long responseTimeMs;
}
