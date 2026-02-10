package com.hackathon.quiz.repository;

import com.hackathon.quiz.entity.Roadmap;
import com.hackathon.quiz.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {
    List<Roadmap> findByUserOrderByWeekNumberAsc(User user);
    void deleteByUser(User user);
}
