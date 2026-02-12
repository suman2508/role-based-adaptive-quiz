package com.hackathon.quiz.service.impl;

import com.hackathon.quiz.dto.response.PracticeScheduleResponse;
import com.hackathon.quiz.dto.response.PerformanceResponse;
import com.hackathon.quiz.entity.PracticeSchedule;
import com.hackathon.quiz.entity.Role;
import com.hackathon.quiz.entity.Skill;
import com.hackathon.quiz.entity.User;
import com.hackathon.quiz.exception.ResourceNotFoundException;
import com.hackathon.quiz.repository.PracticeScheduleRepository;
import com.hackathon.quiz.repository.RoleRepository;
import com.hackathon.quiz.repository.SkillRepository;
import com.hackathon.quiz.repository.UserRepository;
import com.hackathon.quiz.service.PerformanceService;
import com.hackathon.quiz.service.ScheduleService;
import com.hackathon.quiz.validation.ScheduleRequestValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleServiceImpl implements ScheduleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SkillRepository skillRepository;
    private final PracticeScheduleRepository practiceScheduleRepository;
    private final PerformanceService performanceService;
    private final ScheduleRequestValidator scheduleRequestValidator;

    @Override
    public List<PracticeScheduleResponse> getSchedule(Long userId, LocalDate start, LocalDate end) {
        // Defaults to next 14 days
        LocalDate today = LocalDate.now();
        LocalDate startDate = (start != null) ? start : today;
        LocalDate endDate = (end != null) ? end : today.plusDays(13);

        // Guardrails
        scheduleRequestValidator.validateGenerate(userId, startDate, endDate, 45);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + userId));

        // Compute candidate skills
        List<Skill> candidateSkills = resolveCandidateSkills(user);

        // If nothing to schedule, return existing schedules (or empty)
        if (candidateSkills.isEmpty()) {
            return practiceScheduleRepository.findByUserAndDateBetweenOrderByDateAsc(user, startDate, endDate)
                    .stream().map(this::toResponse).toList();
        }

        // Build ranking using performance (weaker skills first), then role priority
        Map<Long, Double> weaknessScore = buildWeaknessScore(userId, candidateSkills);

        // Order skills: higher weakness first, then higher role priority
        List<Skill> ordered = candidateSkills.stream()
                .sorted((a, b) -> {
                    double wa = weaknessScore.getOrDefault(a.getId(), 1.0); // default weak
                    double wb = weaknessScore.getOrDefault(b.getId(), 1.0);
                    int cmp = Double.compare(wb, wa); // desc weakness
                    if (cmp != 0) return cmp;
                    int pa = a.getPriorityScore() != null ? a.getPriorityScore() : 0;
                    int pb = b.getPriorityScore() != null ? b.getPriorityScore() : 0;
                    return Integer.compare(pb, pa); // desc priority
                })
                .toList();

        // Idempotent generation for the range: wipe and re-create for this window
        practiceScheduleRepository.deleteByUserAndDateBetween(user, startDate, endDate);

        // Round-robin assignment to ensure coverage and avoid repetition
        List<PracticeSchedule> buffer = new ArrayList<>();
        long days = endDate.toEpochDay() - startDate.toEpochDay() + 1;
        int idx = 0;
        for (int d = 0; d < days; d++) {
            LocalDate day = startDate.plusDays(d);
            Skill s = ordered.get(idx % ordered.size());
            idx++;

            String activity = buildActivityForSkill(s, d);

            PracticeSchedule ps = PracticeSchedule.builder()
                    .user(user)
                    .date(day)
                    .skill(s)
                    .activityDescription(activity)
                    .build();
            buffer.add(ps);
        }

        List<PracticeSchedule> saved = practiceScheduleRepository.saveAll(buffer);

        return saved.stream()
                .sorted(Comparator.comparing(PracticeSchedule::getDate))
                .map(this::toResponse)
                .toList();
    }

    private List<Skill> resolveCandidateSkills(User user) {
        if (user.getTargetRole() == null || user.getTargetRole().isBlank()) {
            // Without target role, we can't map to required skills reliably
            return Collections.emptyList();
        }
        Role role = roleRepository.findByRoleName(user.getTargetRole())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found by name: " + user.getTargetRole()));
        // Higher priority first
        return skillRepository.findByRoleOrderByPriorityScoreAsc(role).stream()
                .sorted(Comparator.comparing(Skill::getPriorityScore, Comparator.nullsFirst(Integer::compareTo)).reversed())
                .toList();
    }

    /**
     * Builds a weakness score in [0,1+] where higher means "weaker, prioritize sooner".
     * Heuristic: weakness = (1 - accuracy) * 0.7 + (1 - recentAvg) * 0.3.
     * If no attempts, treat as fully weak (1.0).
     */
    private Map<Long, Double> buildWeaknessScore(Long userId, List<Skill> skills) {
        PerformanceResponse performance = performanceService.getPerformance(userId);
        Map<String, PerformanceResponse.PerSkill> byName = Optional.ofNullable(performance.getSkills())
                .orElseGet(Collections::emptyList).stream()
                .filter(ps -> ps.getSkillName() != null)
                .collect(Collectors.toMap(PerformanceResponse.PerSkill::getSkillName, ps -> ps, (a, b) -> a));

        Map<Long, Double> score = new HashMap<>();
        for (Skill s : skills) {
            PerformanceResponse.PerSkill ps = byName.get(s.getSkillName());
            double weakness;
            if (ps == null) {
                weakness = 1.0; // unseen -> prioritize
            } else {
                double acc = clamp01(ps.getAccuracy());
                double recent = clamp01(ps.getRecentAvg());
                weakness = (1.0 - acc) * 0.7 + (1.0 - recent) * 0.3;
            }
            score.put(s.getId(), weakness);
        }
        return score;
    }

    private String buildActivityForSkill(Skill skill, int dayIndex) {
        // Simple rotating template; can be extended by LLM suggestions in future
        String base = "Practice 60 mins: %s — 10 MCQs, 1 coding task, and review notes.";
        String name = skill != null ? skill.getSkillName() : "General";
        return String.format(base, name);
    }

    private double clamp01(double v) {
        if (v < 0.0) return 0.0;
        if (v > 1.0) return 1.0;
        return v;
    }

    private PracticeScheduleResponse toResponse(PracticeSchedule ps) {
        return PracticeScheduleResponse.builder()
                .id(ps.getId())
                .userId(ps.getUser() != null ? ps.getUser().getId() : null)
                .date(ps.getDate())
                .skillId(ps.getSkill() != null ? ps.getSkill().getId() : null)
                .skillName(ps.getSkill() != null ? ps.getSkill().getSkillName() : null)
                .activityDescription(ps.getActivityDescription())
                .build();
    }
}
