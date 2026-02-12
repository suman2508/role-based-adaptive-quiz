package com.hackathon.quiz.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.quiz.dto.response.QuizQuestionResponse;
import com.hackathon.quiz.entity.QuizQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Mapper for quiz entities to DTOs. Uses Jackson for options JSON parsing.
 */
@Component
@RequiredArgsConstructor
public class QuizMapper {

    private final ObjectMapper objectMapper;

    public QuizQuestionResponse toResponse(QuizQuestion q) {
        if (q == null) return null;
        return QuizQuestionResponse.builder()
                .id(q.getId())
                .skillId(q.getSkill() != null ? q.getSkill().getId() : null)
                .skillName(q.getSkill() != null ? q.getSkill().getSkillName() : null)
                .difficulty(q.getDifficulty())
                .questionText(q.getQuestionText())
                .options(parseOptions(q.getOptions()))
                .explanation(q.getExplanation())
                .build();
    }

    public List<String> parseOptions(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
