package com.hackathon.quiz.ai.service.impl;

import com.hackathon.quiz.ai.AiService;
import com.hackathon.quiz.ai.service.QuizGenerationAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Adapter wrapping low-level AiService for quiz question generation use cases.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuizGenerationAIServiceImpl implements QuizGenerationAIService {

    private final AiService aiService;

    @Override
    public String generateQuestionsJson(String skillName, String difficulty) {
        log.debug("QuizGenerationAIService.generateQuestionsJson skill={}, difficulty={}", skillName, difficulty);
        return aiService.generateQuizQuestionJson(skillName, difficulty);
    }
}
