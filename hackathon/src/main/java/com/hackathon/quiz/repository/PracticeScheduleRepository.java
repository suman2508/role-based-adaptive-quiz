package com.hackathon.quiz.repository;

import com.hackathon.quiz.entity.PracticeSchedule;
import com.hackathon.quiz.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PracticeScheduleRepository extends JpaRepository<PracticeSchedule, Long> {
    List<PracticeSchedule> findByUserAndDateBetweenOrderByDateAsc(User user, LocalDate from, LocalDate to);
    long deleteByUserAndDateBetween(User user, LocalDate from, LocalDate to);
}
