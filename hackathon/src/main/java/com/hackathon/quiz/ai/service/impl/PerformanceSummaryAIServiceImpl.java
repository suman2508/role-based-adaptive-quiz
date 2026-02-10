package com.hackathon.quiz.ai.service.impl;

import com.hackathon.quiz.ai.AiService;
import com.hackathon.quiz.ai.service.PerformanceSummaryAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Adapter wrapping low-level AiService for performance summary generation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PerformanceSummaryAIServiceImpl implements PerformanceSummaryAIService {

    private final AiService aiService;

    @Override
    public String generateSummaryJson(String roleName, String performanceTable) {
        log.debug("PerformanceSummaryAIService.generateSummaryJson role={}", roleName);
        return aiService.generatePerformanceSummaryJson(roleName, performanceTable);
    }
}
