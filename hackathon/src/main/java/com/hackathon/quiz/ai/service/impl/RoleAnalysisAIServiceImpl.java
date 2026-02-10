package com.hackathon.quiz.ai.service.impl;

import com.hackathon.quiz.ai.AiService;
import com.hackathon.quiz.ai.model.AiSkill;
import com.hackathon.quiz.ai.service.RoleAnalysisAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Adapter that wraps the low-level AiService for role analysis use cases.
 * Keeps domain services decoupled from the concrete AI provider.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleAnalysisAIServiceImpl implements RoleAnalysisAIService {

    private final AiService aiService;

    @Override
    public List<AiSkill> extractSkills(String roleName) {
        log.debug("RoleAnalysisAIService.extractSkills role={}", roleName);
        return aiService.extractSkillsForRole(roleName);
    }
}
