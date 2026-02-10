package com.hackathon.quiz.ai.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.quiz.ai.AiService;
import com.hackathon.quiz.ai.model.AiSkill;
import com.hackathon.quiz.ai.prompt.PromptTemplates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<AiSkill> extractSkillsForRole(String roleName) {
        String prompt = PromptTemplates.roleToSkillsPrompt(roleName);
        log.debug("AI role->skills prompt for role={}", roleName);
        String content = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        try {
            return objectMapper.readValue(content, new TypeReference<List<AiSkill>>() {});
        } catch (Exception ex) {
            log.error("Failed parsing AI skills JSON for role={}, content={}", roleName, content, ex);
            throw new IllegalStateException("AI response parsing failed for role skills");
        }
    }

    @Override
    public String generateQuizQuestionJson(String skillName, String difficulty) {
        String prompt = PromptTemplates.quizQuestionPrompt(skillName, difficulty);
        log.debug("AI quiz generation for skill={}, difficulty={}", skillName, difficulty);
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    @Override
    public String generatePerformanceSummaryJson(String roleName, String performanceTable) {
        String prompt = PromptTemplates.performanceSummaryPrompt(roleName, performanceTable);
        log.debug("AI performance summary for role={}", roleName);
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
