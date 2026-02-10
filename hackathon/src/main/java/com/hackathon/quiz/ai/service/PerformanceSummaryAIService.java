package com.hackathon.quiz.ai.service;

/**
 * High-level AI performance summary generator.
 * Accepts tabular/JSON performance inputs and returns provider-agnostic JSON summary.
 */
public interface PerformanceSummaryAIService {

    /**
     * Generates a natural language performance summary JSON for a given role and user performance table.
     * The JSON should include: summary, strengths, weaknesses, suggestedNextSteps.
     */
    String generateSummaryJson(String roleName, String performanceTable);
}
