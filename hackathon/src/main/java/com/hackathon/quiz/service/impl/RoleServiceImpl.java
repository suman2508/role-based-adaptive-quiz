package com.hackathon.quiz.service.impl;

import com.hackathon.quiz.ai.model.AiSkill;
import com.hackathon.quiz.ai.service.RoleAnalysisAIService;
import com.hackathon.quiz.dto.request.RoleAnalyzeRequest;
import com.hackathon.quiz.dto.response.RoleAnalyzeResponse;
import com.hackathon.quiz.dto.response.SkillResponse;
import com.hackathon.quiz.entity.Role;
import com.hackathon.quiz.entity.Skill;
import com.hackathon.quiz.exception.ResourceNotFoundException;
import com.hackathon.quiz.mapper.SkillMapper;
import com.hackathon.quiz.repository.RoleRepository;
import com.hackathon.quiz.repository.SkillRepository;
import com.hackathon.quiz.service.RoleService;
import com.hackathon.quiz.validation.RoleRequestValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleAnalysisAIService roleAnalysisAIService;
    private final RoleRepository roleRepository;
    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;
    private final RoleRequestValidator roleRequestValidator;

    @Override
    public RoleAnalyzeResponse analyzeRole(RoleAnalyzeRequest request) {
        roleRequestValidator.validate(request);

        String roleName = request.getRoleName().trim();
        log.info("Analyze role requested: {}", roleName);

        // 1) Call AI to extract skills
        List<AiSkill> aiSkills = roleAnalysisAIService.extractSkills(roleName);
        aiSkills = aiSkills.stream()
                .sorted(Comparator.comparingInt(AiSkill::priorityScore).reversed())
                .toList();

        if (request.getTopN() != null && request.getTopN() > 0 && request.getTopN() < aiSkills.size()) {
            aiSkills = aiSkills.subList(0, request.getTopN());
        }

        // 2) Upsert role
        Role role = roleRepository.findByRoleName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder().roleName(roleName).build()));

        // 3) Replace existing skills for this role with new set
        log.debug("Replacing skills for roleId={} name={}", role.getId(), role.getRoleName());
        skillRepository.deleteByRole(role);

        List<Skill> toSave = aiSkills.stream()
                .map(s -> Skill.builder()
                        .skillName(s.name())
                        .priorityScore(s.priorityScore())
                        .role(role)
                        .build())
                .toList();

        List<Skill> saved = skillRepository.saveAll(toSave);

        // 4) Build response
        List<SkillResponse> skillsResp = saved.stream()
                .sorted(Comparator.comparing(Skill::getPriorityScore).reversed())
                .map(skillMapper::toResponse)
                .toList();

        return RoleAnalyzeResponse.builder()
                .roleId(role.getId())
                .roleName(role.getRoleName())
                .skills(skillsResp)
                .build();
    }

    @Override
    public List<SkillResponse> getSkillsByRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: id=" + roleId));

        return skillRepository.findByRoleOrderByPriorityScoreAsc(role).stream()
                .sorted(Comparator.comparing(Skill::getPriorityScore).reversed())
                .map(skillMapper::toResponse)
                .toList();
    }
}
