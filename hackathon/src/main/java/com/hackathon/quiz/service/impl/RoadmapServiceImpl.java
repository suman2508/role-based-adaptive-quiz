package com.hackathon.quiz.service.impl;

import com.hackathon.quiz.dto.response.RoadmapItemResponse;
import com.hackathon.quiz.entity.Roadmap;
import com.hackathon.quiz.entity.Role;
import com.hackathon.quiz.entity.Skill;
import com.hackathon.quiz.entity.User;
import com.hackathon.quiz.exception.ResourceNotFoundException;
import com.hackathon.quiz.repository.RoadmapRepository;
import com.hackathon.quiz.repository.RoleRepository;
import com.hackathon.quiz.repository.SkillRepository;
import com.hackathon.quiz.repository.UserRepository;
import com.hackathon.quiz.service.RoadmapService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RoadmapServiceImpl implements RoadmapService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SkillRepository skillRepository;
    private final RoadmapRepository roadmapRepository;

    @Override
    public List<RoadmapItemResponse> generateForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + userId));

        if (user.getTargetRole() == null || user.getTargetRole().isBlank()) {
            throw new ResourceNotFoundException("User does not have a targetRole set.");
        }

        Role role = roleRepository.findByRoleName(user.getTargetRole())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found by name: " + user.getTargetRole()));

        List<Skill> skills = skillRepository.findByRoleOrderByPriorityScoreAsc(role).stream()
                .sorted(Comparator.comparing(Skill::getPriorityScore).reversed())
                .toList();

        // Replace existing roadmap for the user
        roadmapRepository.deleteByUser(user);

        int week = 1;
        for (Skill skill : skills) {
            String desc = "Week " + week + ": Focus on " + skill.getSkillName() + " (priority " + skill.getPriorityScore() + "). "
                    + "Study fundamentals, practice exercises, and complete a mini-project if time allows.";
            Roadmap item = Roadmap.builder()
                    .user(user)
                    .weekNumber(week)
                    .skill(skill)
                    .description(desc)
                    .build();
            roadmapRepository.save(item);
            week++;
        }

        return roadmapRepository.findByUserOrderByWeekNumberAsc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<RoadmapItemResponse> getForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + userId));
        return roadmapRepository.findByUserOrderByWeekNumberAsc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    private RoadmapItemResponse toResponse(Roadmap r) {
        return RoadmapItemResponse.builder()
                .id(r.getId())
                .weekNumber(r.getWeekNumber())
                .skillId(r.getSkill() != null ? r.getSkill().getId() : null)
                .skillName(r.getSkill() != null ? r.getSkill().getSkillName() : null)
                .description(r.getDescription())
                .build();
    }
}
