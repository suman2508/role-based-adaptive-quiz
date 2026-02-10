package com.hackathon.quiz.ai.service;

/**
 * High-level AI quiz generation service used by domain services.
 * Returns provider-agnostic JSON that downstream services parse to DTOs/entities.
 */
public interface QuizGenerationAIService {

    /**
     * Generates quiz question(s) JSON for a given skill and difficulty.
     * The JSON schema should include: questionText, options, correctAnswer, explanation, difficulty.
     */
    String generateQuestionsJson(String skillName, String difficulty);
}
