package com.hackathon.quiz.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.quiz.ai.service.PerformanceSummaryAIService;
import com.hackathon.quiz.dto.response.PerformanceResponse;
import com.hackathon.quiz.entity.QuizAttempt;
import com.hackathon.quiz.entity.Role;
import com.hackathon.quiz.entity.Skill;
import com.hackathon.quiz.entity.User;
import com.hackathon.quiz.exception.ResourceNotFoundException;
import com.hackathon.quiz.repository.QuizAttemptRepository;
import com.hackathon.quiz.repository.RoleRepository;
import com.hackathon.quiz.repository.SkillRepository;
import com.hackathon.quiz.repository.UserRepository;
import com.hackathon.quiz.service.PerformanceService;
import com.hackathon.quiz.util.ScoreCalculator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PerformanceServiceImpl implements PerformanceService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SkillRepository skillRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final PerformanceSummaryAIService performanceSummaryAIService;
    private final ObjectMapper objectMapper;

    @Override
    public PerformanceResponse getPerformance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + userId));

        String roleName = user.getTargetRole();
        if (roleName == null || roleName.isBlank()) {
            // No target role => no required skill set; compute based on attempts only
            return buildResponseWithoutRole(user);
        }

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found by name: " + roleName));

        List<Skill> requiredSkills = skillRepository.findByRoleOrderByPriorityScoreAsc(role);
        Map<Long, Skill> skillById = requiredSkills.stream().collect(Collectors.toMap(Skill::getId, s -> s));

        List<QuizAttempt> allAttempts = quizAttemptRepository.findByUserOrderByAttemptTimestampDesc(user);

        // Group attempts by skill
        Map<Long, List<QuizAttempt>> attemptsBySkill = new HashMap<>();
        for (QuizAttempt a : allAttempts) {
            if (a.getQuestion() == null || a.getQuestion().getSkill() == null) continue;
            Long sid = a.getQuestion().getSkill().getId();
            attemptsBySkill.computeIfAbsent(sid, k -> new ArrayList<>()).add(a);
        }

        // Build per-skill stats for required skills
        List<PerformanceResponse.PerSkill> perSkills = new ArrayList<>();
        for (Skill s : requiredSkills) {
            List<QuizAttempt> list = attemptsBySkill.getOrDefault(s.getId(), Collections.emptyList());
            int attempts = list.size();
            double accuracy = attempts == 0 ? 0.0 : list.stream()
                    .map(a -> a.getScore() != null ? a.getScore() : 0.0)
                    .mapToDouble(Double::doubleValue).average().orElse(0.0);

            // Recent moving average over last 5
            List<Double> lastScores = list.stream()
                    .limit(5L)
                    .map(a -> a.getScore() != null ? a.getScore() : 0.0)
                    .toList();
            double recentAvg = ScoreCalculator.movingAverage(lastScores, Math.min(5, lastScores.size()));

            perSkills.add(PerformanceResponse.PerSkill.builder()
                    .skillId(s.getId())
                    .skillName(s.getSkillName())
                    .attempts(attempts)
                    .accuracy(accuracy)
                    .recentAvg(recentAvg)
                    .build());
        }

        // Coverage ratio: required skills with >=1 attempt / total required
        long covered = perSkills.stream().filter(ps -> ps.getAttempts() > 0).count();
        double coverageRatio = requiredSkills.isEmpty() ? 0.0 : (double) covered / (double) requiredSkills.size();

        // Overall performance: weighted by attempts
        double overallPerformance = weightedAccuracy(perSkills);

        // Recency boost: moving average over last 10 attempts overall
        List<Double> overallLast = allAttempts.stream()
                .limit(10L)
                .map(a -> a.getScore() != null ? a.getScore() : 0.0)
                .toList();
        double recencyBoost = ScoreCalculator.movingAverage(overallLast, Math.min(10, overallLast.size()));

        double readiness = ScoreCalculator.readinessScore(coverageRatio, overallPerformance, recencyBoost);
        int readinessPercent = ScoreCalculator.toPercent(readiness);

        // Optional AI summary
        String aiSummary = null;
        try {
            String table = toPerformanceTable(perSkills);
            String raw = performanceSummaryAIService.generateSummaryJson(roleName, table);
            aiSummary = extractSummaryField(raw);
        } catch (Exception ignored) {
            // AI summary is optional; ignore failures
        }

        return PerformanceResponse.builder()
                .userId(user.getId())
                .targetRole(roleName)
                .readinessScore(readiness)
                .readinessPercent(readinessPercent)
                .skills(perSkills)
                .aiSummary(aiSummary)
                .build();
    }

    @Override
    public double getReadinessScore(Long userId) {
        PerformanceResponse pr = getPerformance(userId);
        return pr.getReadinessScore();
    }

    private PerformanceResponse buildResponseWithoutRole(User user) {
        List<QuizAttempt> allAttempts = quizAttemptRepository.findByUserOrderByAttemptTimestampDesc(user);

        // Group by skill name for visibility even without a declared role
        Map<String, List<QuizAttempt>> bySkillName = new HashMap<>();
        for (QuizAttempt a : allAttempts) {
            String name = (a.getQuestion() != null && a.getQuestion().getSkill() != null)
                    ? a.getQuestion().getSkill().getSkillName()
                    : "General";
            bySkillName.computeIfAbsent(name, k -> new ArrayList<>()).add(a);
        }

        List<PerformanceResponse.PerSkill> perSkills = new ArrayList<>();
        for (Map.Entry<String, List<QuizAttempt>> e : bySkillName.entrySet()) {
            List<QuizAttempt> list = e.getValue();
            int attempts = list.size();
            double accuracy = attempts == 0 ? 0.0 : list.stream()
                    .map(a -> a.getScore() != null ? a.getScore() : 0.0)
                    .mapToDouble(Double::doubleValue).average().orElse(0.0);
            List<Double> last = list.stream().limit(5L).map(a -> a.getScore() != null ? a.getScore() : 0.0).toList();
            double recentAvg = ScoreCalculator.movingAverage(last, Math.min(5, last.size()));

            perSkills.add(PerformanceResponse.PerSkill.builder()
                    .skillId(null)
                    .skillName(e.getKey())
                    .attempts(attempts)
                    .accuracy(accuracy)
                    .recentAvg(recentAvg)
                    .build());
        }

        // Fallback readiness based on overall accuracy and recency only
        List<Double> overallLast = allAttempts.stream()
                .limit(10L)
                .map(a -> a.getScore() != null ? a.getScore() : 0.0)
                .toList();
        double recencyBoost = ScoreCalculator.movingAverage(overallLast, Math.min(10, overallLast.size()));
        double performance = weightedAccuracy(perSkills);
        double readiness = ScoreCalculator.readinessScore(0.0, performance, recencyBoost);
        int readinessPercent = ScoreCalculator.toPercent(readiness);

        return PerformanceResponse.builder()
                .userId(user.getId())
                .targetRole(null)
                .readinessScore(readiness)
                .readinessPercent(readinessPercent)
                .skills(perSkills)
                .aiSummary(null)
                .build();
    }

    private double weightedAccuracy(List<PerformanceResponse.PerSkill> perSkills) {
        if (perSkills == null || perSkills.isEmpty()) return 0.0;
        double vsum = 0.0;
        double wsum = 0.0;
        for (PerformanceResponse.PerSkill ps : perSkills) {
            int w = Math.max(ps.getAttempts(), 1);
            vsum += ps.getAccuracy() * w;
            wsum += w;
        }
        return wsum == 0.0 ? 0.0 : vsum / wsum;
    }

    private String toPerformanceTable(List<PerformanceResponse.PerSkill> perSkills) {
        StringBuilder sb = new StringBuilder();
        sb.append("Skill | Attempts | Accuracy | RecentAvg\n");
        sb.append("----- | -------- | -------- | --------\n");
        for (PerformanceResponse.PerSkill ps : perSkills) {
            sb.append(safe(ps.getSkillName())).append(" | ")
              .append(ps.getAttempts()).append(" | ")
              .append(String.format(Locale.ROOT, "%.2f", ps.getAccuracy())).append(" | ")
              .append(String.format(Locale.ROOT, "%.2f", ps.getRecentAvg())).append("\n");
        }
        return sb.toString();
    }

    private String extractSummaryField(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) return null;
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            if (root.hasNonNull("summary")) {
                return root.get("summary").asText();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String safe(String s) {
        return s == null ? "" : s.replace("|", "/");
    }
}
