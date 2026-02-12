package com.hackathon.quiz.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class QuizSubmitRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Long questionId;

    @NotNull
    @Size(min = 1, max = 512)
    private String selectedAnswer;
}
