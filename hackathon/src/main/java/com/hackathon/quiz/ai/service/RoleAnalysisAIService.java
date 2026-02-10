package com.hackathon.quiz.ai.service;

import com.hackathon.quiz.ai.model.AiSkill;

import java.util.List;

/**
 * High-level AI role analysis service used by domain services.
 * This wraps provider specifics to keep service layer decoupled from AI vendor APIs.
 */
public interface RoleAnalysisAIService {

    /**
     * Extracts skills for a given role name using LLM.
     */
    List<AiSkill> extractSkills(String roleName);
}
