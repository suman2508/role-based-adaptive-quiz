package com.hackathon.quiz.validation;

import com.hackathon.quiz.exception.ValidationException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Validator for practice schedule generation requests.
 * Keeps date logic and guardrails centralized.
 */
public class ScheduleRequestValidator {

    /**
     * Validates schedule generation inputs.
     * @param userId target user id (required, >0)
     * @param start  start date (inclusive), must not be null and not far in past
     * @param end    end date (inclusive), must not be before start and within sane window
     * @param maxWindowDays maximum allowed range size (e.g., 45)
     */
    public void validateGenerate(Long userId, LocalDate start, LocalDate end, int maxWindowDays) {
        List<String> details = new ArrayList<>();

        if (userId == null || userId <= 0) {
            details.add("userId: must be a positive id");
        }
        if (start == null) {
            details.add("startDate: must not be null");
        }
        if (end == null) {
            details.add("endDate: must not be null");
        }

        if (start != null && end != null) {
            if (end.isBefore(start)) {
                details.add("endDate: must be on/after startDate");
            }
            int days = (int) (end.toEpochDay() - start.toEpochDay()) + 1;
            if (days <= 0) {
                details.add("dateRange: must be at least 1 day");
            }
            if (maxWindowDays > 0 && days > maxWindowDays) {
                details.add("dateRange: must be <= " + maxWindowDays + " days");
            }
        }

        if (!details.isEmpty()) {
            throw new ValidationException("VALIDATION_ERROR", "Invalid schedule generation request", details);
        }
    }
}
