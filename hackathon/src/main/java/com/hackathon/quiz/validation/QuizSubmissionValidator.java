package com.hackathon.quiz.validation;

import com.hackathon.quiz.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Validator for quiz submission/start requests.
 * Keeps controllers lean and centralizes complex validation logic.
 */
@Component
public class QuizSubmissionValidator {

    /**
     * Validates a quiz submission with optional allowed options list.
     */
    public void validateSubmission(Long userId,
                                   Long questionId,
                                   String selectedAnswer,
                                   List<String> allowedOptions) {
        List<String> details = new ArrayList<>();

        if (userId == null || userId <= 0) {
            details.add("userId: must be a positive id");
        }
        if (questionId == null || questionId <= 0) {
            details.add("questionId: must be a positive id");
        }
        if (selectedAnswer == null || selectedAnswer.trim().isEmpty()) {
            details.add("selectedAnswer: must not be blank");
        } else if (selectedAnswer.length() > 512) {
            details.add("selectedAnswer: length must be <= 512");
        }

        if (allowedOptions != null && !allowedOptions.isEmpty()) {
            boolean present = allowedOptions.stream()
                    .filter(opt -> opt != null)
                    .anyMatch(opt -> opt.equals(selectedAnswer));
            if (!present) {
                details.add("selectedAnswer: must be one of the provided options");
            }
        }

        if (!details.isEmpty()) {
            throw new ValidationException("VALIDATION_ERROR", "Invalid quiz submission", details);
        }
    }

    /**
     * Validates a request to start a quiz session (basic variant).
     * Extend with role/skill filters as needed.
     */
    public void validateStart(Long userId) {
        List<String> details = new ArrayList<>();
        if (userId == null || userId <= 0) {
            details.add("userId: must be a positive id");
        }
        if (!details.isEmpty()) {
            throw new ValidationException("VALIDATION_ERROR", "Invalid start quiz request", details);
        }
    }
}
