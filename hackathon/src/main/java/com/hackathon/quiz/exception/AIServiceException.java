package com.hackathon.quiz.exception;

/**
 * Thrown when the AI provider or parsing of AI output fails.
 * Distinct from BusinessException and ValidationException to aid observability and retries.
 */
public class AIServiceException extends RuntimeException {
    private final String provider;

    public AIServiceException(String message) {
        super(message);
        this.provider = null;
    }

    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
        this.provider = null;
    }

    public AIServiceException(String provider, String message, Throwable cause) {
        super(message, cause);
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }
}
