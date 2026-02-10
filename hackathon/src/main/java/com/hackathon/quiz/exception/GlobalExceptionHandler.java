package com.hackathon.quiz.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Global exception translator to standardized API error responses.
 * - Uses RFC 7807-inspired fields with custom 'code' for client branching.
 * - Ensures minimal leakage of internals while preserving actionable messages.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class GlobalExceptionHandler {

    // ========= 404 family =========

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException ex,
                                                                   HttpServletRequest request) {
        ApiErrorResponse body = base(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), null, request);
        log.debug("NOT_FOUND: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoHandlerFound(NoHandlerFoundException ex,
                                                                 HttpServletRequest request) {
        String msg = "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL();
        ApiErrorResponse body = base(HttpStatus.NOT_FOUND, "NO_HANDLER_FOUND", msg, null, request);
        log.debug("NO_HANDLER_FOUND: {}", msg);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // ========= 400 validation / request errors =========

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                         HttpServletRequest request) {
        List<String> details = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            details.add(fe.getField() + ": " + fe.getDefaultMessage());
        }
        ex.getBindingResult().getGlobalErrors()
                .forEach(err -> details.add(err.getObjectName() + ": " + err.getDefaultMessage()));

        ApiErrorResponse body = base(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Validation failed for request", details, request);
        log.debug("VALIDATION_ERROR: {}", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleBindException(BindException ex,
                                                                HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.toList());
        ApiErrorResponse body = base(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Binding/validation failed", details, request);
        log.debug("VALIDATION_ERROR(bind): {}", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                      HttpServletRequest request) {
        Set<String> details = ex.getConstraintViolations().stream()
                .map(v -> (v.getPropertyPath() != null ? v.getPropertyPath().toString() : "value") + ": " + v.getMessage())
                .collect(Collectors.toSet());
        ApiErrorResponse body = base(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Constraint violations", new ArrayList<>(details), request);
        log.debug("VALIDATION_ERROR(constraint): {}", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                               HttpServletRequest request) {
        String required = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String msg = "Parameter '" + ex.getName() + "' type mismatch. Required type: " + required;
        ApiErrorResponse body = base(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", msg, null, request);
        log.debug("TYPE_MISMATCH: {}", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParam(MissingServletRequestParameterException ex,
                                                               HttpServletRequest request) {
        String msg = "Missing required parameter: " + ex.getParameterName();
        ApiErrorResponse body = base(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", msg, null, request);
        log.debug("MISSING_PARAMETER: {}", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException ex,
                                                              HttpServletRequest request) {
        ApiErrorResponse body = base(HttpStatus.BAD_REQUEST, "MALFORMED_JSON",
                "Malformed JSON request body", null, request);
        log.debug("MALFORMED_JSON: {}", ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiErrorResponse> handleIllegal(RuntimeException ex,
                                                          HttpServletRequest request) {
        ApiErrorResponse body = base(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), null, request);
        log.debug("BAD_REQUEST: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ========= 401/403 security =========

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuth(AuthenticationException ex,
                                                       HttpServletRequest request) {
        ApiErrorResponse body = base(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", ex.getMessage(), null, request);
        log.debug("AUTHENTICATION_FAILED: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex,
                                                               HttpServletRequest request) {
        ApiErrorResponse body = base(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage(), null, request);
        log.debug("ACCESS_DENIED: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // ========= 405/415 method & media =========

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                     HttpServletRequest request) {
        String supported = ex.getSupportedHttpMethods() != null ? ex.getSupportedHttpMethods().toString() : "[]";
        String msg = "HTTP method not supported. Supported: " + supported;
        ApiErrorResponse body = base(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", msg, null, request);
        log.debug("METHOD_NOT_ALLOWED: {}", msg);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                                                                        HttpServletRequest request) {
        String msg = "Media type not supported: " + ex.getContentType();
        ApiErrorResponse body = base(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", msg, null, request);
        log.debug("UNSUPPORTED_MEDIA_TYPE: {}", msg);
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(body);
    }

    // ========= 409 data integrity =========

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                                                                HttpServletRequest request) {
        String msg = "Data integrity violation";
        ApiErrorResponse body = base(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION", msg, null, request);
        log.warn("DATA_INTEGRITY_VIOLATION: {}", ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // ========= propagate ResponseStatusException as-is =========

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex,
                                                                 HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String msg = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        ApiErrorResponse body = base(status, status.name(), msg, null, request);
        if (status.is5xxServerError()) {
            log.error("RSE_5XX: {}", msg, ex);
        } else {
            log.debug("RSE_4XX: {}", msg);
        }
        return ResponseEntity.status(status).body(body);
    }

    // ========= 422/5xx domain-specific =========

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException ex,
                                                           HttpServletRequest request) {
        ApiErrorResponse body = base(HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getCode() != null ? ex.getCode() : "BUSINESS_ERROR",
                ex.getMessage(), null, request);
        log.debug("BUSINESS_ERROR: code={}, msg={}", ex.getCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(ValidationException ex,
                                                             HttpServletRequest request) {
        ApiErrorResponse body = base(HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getCode() != null ? ex.getCode() : "VALIDATION_ERROR",
                ex.getMessage(), ex.getDetails(), request);
        log.debug("VALIDATION_EXCEPTION: code={}, details={}", ex.getCode(), ex.getDetails());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(AIServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleAi(AIServiceException ex,
                                                     HttpServletRequest request) {
        String msg = (ex.getProvider() != null ? "[" + ex.getProvider() + "] " : "") +
                (ex.getMessage() != null ? ex.getMessage() : "AI service error");
        ApiErrorResponse body = base(HttpStatus.BAD_GATEWAY, "AI_SERVICE_ERROR", msg, null, request);
        log.error("AI_SERVICE_ERROR: {}", msg, ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    // ========= 500 fallback =========

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex,
                                                          HttpServletRequest request) {
        ApiErrorResponse body = base(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred", null, request);
        log.error("INTERNAL_ERROR: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // ========= helpers =========

    private ApiErrorResponse base(HttpStatus status,
                                  String code,
                                  String message,
                                  List<String> details,
                                  HttpServletRequest request) {
        String traceId = nullSafe(MDC.get("traceId"));
        String path = request != null ? request.getRequestURI() : null;

        return ApiErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .code(code)
                .path(path)
                .details(details)
                .traceId(traceId)
                .build();
    }

    private static String nullSafe(String val) {
        return (val == null || val.isBlank()) ? null : val;
    }
}
