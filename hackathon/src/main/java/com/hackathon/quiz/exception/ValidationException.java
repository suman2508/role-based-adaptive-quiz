package com.hackathon.quiz.exception;

import java.util.List;

/**
 * Thrown when request/domain validation fails beyond simple annotation-based checks.
 * Carries optional details for precise client feedback.
 */
public class ValidationException extends RuntimeException {
    private final List<String> details;
    private final String code;

    public ValidationException(String message) {
        super(message);
        this.details = null;
        this.code = "VALIDATION_ERROR";
    }

    public ValidationException(String message, List<String> details) {
        super(message);
        this.details = details;
        this.code = "VALIDATION_ERROR";
    }

    public ValidationException(String code, String message, List<String> details) {
        super(message);
        this.details = details;
        this.code = (code == null || code.isBlank()) ? "VALIDATION_ERROR" : code;
    }

    public List<String> getDetails() {
        return details;
    }

    public String getCode() {
        return code;
    }
}
