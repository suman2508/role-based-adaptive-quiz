package com.hackathon.quiz.service;

import com.hackathon.quiz.dto.response.PracticeScheduleResponse;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {
    /**
     * Generates (idempotently for the given range) and returns a user's practice schedule.
     * If start or end is null, defaults to [today, today+13].
     */
    List<PracticeScheduleResponse> getSchedule(Long userId, LocalDate start, LocalDate end);
}
