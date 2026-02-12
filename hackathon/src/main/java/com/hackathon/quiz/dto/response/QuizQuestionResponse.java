package com.hackathon.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO exposed to clients for a quiz question.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionResponse {
    private Long id;
    private Long skillId;
    private String skillName;
    private String difficulty; // easy | medium | hard
    private String questionText;
    private List<String> options; // client-friendly list of options
    private String explanation;   // optional; may be omitted for "question only" views
}
