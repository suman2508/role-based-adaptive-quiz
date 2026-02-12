package com.hackathon.quiz.service;

import com.hackathon.quiz.dto.response.PerformanceResponse;

public interface PerformanceService {
    PerformanceResponse getPerformance(Long userId);
    double getReadinessScore(Long userId);
}
