package com.hackathon.quiz.exception;

/**
 * Thrown when a business rule is violated (distinct from validation or system errors).
 */
public class BusinessException extends RuntimeException {
    private final String code;

    public BusinessException(String message) {
        super(message);
        this.code = "BUSINESS_ERROR";
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code == null || code.isBlank() ? "BUSINESS_ERROR" : code;
    }

    public String getCode() {
        return code;
    }
}
