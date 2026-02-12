package com.hackathon.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Aggregated performance response with readiness score and per-skill stats.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceResponse {

    private Long userId;
    private String targetRole;
    /**
     * Normalized readiness 0..1
     */
    private double readinessScore;
    /**
     * Readiness in percentage 0..100
     */
    private int readinessPercent;

    private List<PerSkill> skills;

    /**
     * Optional natural language summary generated via Spring AI.
     */
    private String aiSummary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerSkill {
        private Long skillId;
        private String skillName;
        private int attempts;
        /**
         * Normalized accuracy 0..1
         */
        private double accuracy;
        /**
         * Recent moving average 0..1 over last N attempts for the skill.
         */
        private double recentAvg;
    }
}
