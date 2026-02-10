package com.hackathon.quiz.ai.prompt;

/**
 * Centralized prompt templates used by the AI integration layer.
 * Keep prompts deterministic and JSON-only where possible for reliable parsing.
 */
public final class PromptTemplates {

    private PromptTemplates() {}

    /**
     * Role -> Skill extraction prompt.
     * Expected JSON array: [{ "name": "Java", "priorityScore": 90 }, ...]
     */
    public static String roleToSkillsPrompt(String roleName) {
        return """
        You are an expert career mentor and skills analyst.
        Analyze the target job role: "%s".
        
        Task:
        - Identify the most important hard and soft skills required for success in this role.
        - Score each skill's importance from 1 to 100 (higher = more critical).
        - Return strictly valid JSON ONLY (no commentary, no markdown).
        
        JSON schema:
        [
          { "name": "string (skill name)", "priorityScore": number (1-100) },
          ...
        ]
        """.formatted(roleName);
    }

    /**
     * Quiz question generation prompt.
     * Expected JSON object with fields:
     * {
     *   "questionText": "string",
     *   "options": ["A", "B", "C", "D"],
     *   "correctAnswer": "A",
     *   "explanation": "string",
     *   "difficulty": "easy|medium|hard"
     * }
     */
    public static String quizQuestionPrompt(String skillName, String difficulty) {
        return """
        You are an AI quiz generator. Create a single multiple-choice question to assess the skill "%s".
        
        Requirements:
        - Difficulty: %s
        - Provide 4 distinct options.
        - Provide one correct answer that exactly matches one of the options.
        - Provide a concise explanation of why the correct answer is correct.
        - Return strictly valid JSON ONLY (no commentary, no markdown).
        
        JSON schema:
        {
          "questionText": "string",
          "options": ["string", "string", "string", "string"],
          "correctAnswer": "string",
          "explanation": "string",
          "difficulty": "easy|medium|hard"
        }
        """.formatted(skillName, difficulty);
    }

    /**
     * Performance summary and gap analysis.
     * Expected JSON:
     * {
     *   "summary": "string",
     *   "recommendations": ["string", ...]
     * }
     */
    public static String performanceSummaryPrompt(String roleName, String performanceTable) {
        return """
        You are a learning coach. Summarize the user's performance relative to role "%s".
        
        Data:
        %s
        
        Produce:
        - Short summary of strengths and weaknesses.
        - 3-6 prioritized recommendations.
        Return strictly valid JSON ONLY (no commentary, no markdown).
        
        JSON schema:
        {
          "summary": "string",
          "recommendations": ["string", "string", ...]
        }
        """.formatted(roleName, performanceTable);
    }
}
