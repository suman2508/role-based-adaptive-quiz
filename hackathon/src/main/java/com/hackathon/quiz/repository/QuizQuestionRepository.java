package com.hackathon.quiz.repository;

import com.hackathon.quiz.entity.QuizQuestion;
import com.hackathon.quiz.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findBySkillAndDifficultyOrderByIdAsc(Skill skill, String difficulty);
    Optional<QuizQuestion> findFirstBySkillAndDifficultyOrderByIdAsc(Skill skill, String difficulty);
    List<QuizQuestion> findBySkillInAndDifficultyOrderByIdAsc(List<Skill> skills, String difficulty);
}
