package com.hackathon.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response after submitting an answer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmitResponse {
    private Long questionId;
    private boolean correct;
    private double score; // 1.0 if correct else 0.0 (or partial in future)
    private String correctAnswer;
    private String explanation;
}
