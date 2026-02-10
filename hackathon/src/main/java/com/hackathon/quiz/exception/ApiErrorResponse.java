package com.hackathon.quiz.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Standard API error payload returned by the GlobalExceptionHandler.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {
    /**
     * RFC 3339 timestamp in UTC.
     */
    @Builder.Default
    private OffsetDateTime timestamp = OffsetDateTime.now();

    /**
     * HTTP status code (e.g., 400, 404).
     */
    private int status;

    /**
     * HTTP reason phrase (e.g., "Bad Request", "Not Found").
     */
    private String error;

    /**
     * Human-readable short error message.
     */
    private String message;

    /**
     * Optional domain/business error code for clients to branch on.
     * Examples:
     * - VALIDATION_ERROR
     * - NOT_FOUND
     * - CONSTRAINT_VIOLATION
     * - METHOD_NOT_ALLOWED
     * - UNSUPPORTED_MEDIA_TYPE
     * - AUTHENTICATION_FAILED
     * - ACCESS_DENIED
     * - DATA_INTEGRITY_VIOLATION
     * - INTERNAL_ERROR
     */
    private String code;

    /**
     * Request path where the error occurred.
     */
    private String path;

    /**
     * Optional details such as field validation messages.
     */
    private List<String> details;

    /**
     * Optional correlation/trace id for log correlation.
     */
    private String traceId;
}
