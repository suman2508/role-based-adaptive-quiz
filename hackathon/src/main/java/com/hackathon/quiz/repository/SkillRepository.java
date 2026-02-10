package com.hackathon.quiz.repository;

import com.hackathon.quiz.entity.Role;
import com.hackathon.quiz.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByRoleOrderByPriorityScoreAsc(Role role);
    void deleteByRole(Role role);
}
