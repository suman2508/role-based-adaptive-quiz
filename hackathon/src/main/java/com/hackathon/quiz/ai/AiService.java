package com.hackathon.quiz.ai;

import com.hackathon.quiz.ai.model.AiSkill;

import java.util.List;

/**
 * Port for AI operations used across the platform.
 * Implementations should be stateless and idempotent given same inputs.
 */
public interface AiService {

    /**
     * Extract and rank skills for a given role using LLM.
     * @param roleName target role (e.g., "Backend Developer")
     * @return ordered list of skills with priority scores (desc by importance recommended by caller)
     */
    List<AiSkill> extractSkillsForRole(String roleName);

    /**
     * Generate a single MCQ for a given skill and difficulty.
     * The returned JSON should contain: questionText, options[], correctAnswer, explanation, difficulty.
     * This method should return the raw JSON string; mapping to DTO/entity is handled by caller to allow flexibility.
     * @param skillName skill/topic to assess
     * @param difficulty easy|medium|hard
     * @return raw JSON string representing the question
     */
    String generateQuizQuestionJson(String skillName, String difficulty);

    /**
     * Generate performance summary and recommendations as raw JSON,
     * based on a table-like input assembled by analytics (e.g., CSV or markdown table).
     * Caller parses the JSON into typed DTO.
     * @param roleName target role reference
     * @param performanceTable text table with per-skill accuracy, counts, etc.
     * @return raw JSON string with fields: summary, recommendations[]
     */
    String generatePerformanceSummaryJson(String roleName, String performanceTable);
}
