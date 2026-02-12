package com.hackathon.quiz.repository;

import com.hackathon.quiz.entity.QuizAttempt;
import com.hackathon.quiz.entity.Skill;
import com.hackathon.quiz.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findTop10ByUserOrderByAttemptTimestampDesc(User user);
    List<QuizAttempt> findByUserOrderByAttemptTimestampDesc(User user);
    List<QuizAttempt> findByUserAndQuestion_SkillOrderByAttemptTimestampDesc(User user, Skill skill);
    long deleteByAttemptTimestampBefore(OffsetDateTime cutoff);
}
