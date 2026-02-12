package com.hackathon.quiz.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request to start a quiz session (stateless variant).
 * You can extend with filters (roleId, skillId) as needed.
 */
@Data
public class QuizStartRequest {

    @NotNull
    private Long userId;

    /**
     * Preferred difficulty: easy|medium|hard (case-insensitive). Optional; defaults to medium.
     */
    private String difficulty;

    /**
     * Optional: limit initial number of questions to return (default 5).
     */
    @Min(1)
    private Integer limit;

    /**
     * Optional: narrow down to a specific skill id.
     */
    private Long skillId;
}
