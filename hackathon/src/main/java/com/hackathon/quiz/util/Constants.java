package com.hackathon.quiz.util;

/**
 * Central application constants. Prefer enums for categorical values.
 */
public final class Constants {
    private Constants() {}

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    // Pagination defaults
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // AI
    public static final int DEFAULT_TOP_SKILLS = 20;
    public static final String AI_PROVIDER_OPENAI = "openai";

    // Security
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";
}
