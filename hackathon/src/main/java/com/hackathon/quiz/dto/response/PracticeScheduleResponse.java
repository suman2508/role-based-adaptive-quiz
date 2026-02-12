package com.hackathon.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for daily practice schedule items.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeScheduleResponse {
    private Long id;
    private Long userId;
    private LocalDate date;
    private Long skillId;
    private String skillName;
    private String activityDescription;
}
