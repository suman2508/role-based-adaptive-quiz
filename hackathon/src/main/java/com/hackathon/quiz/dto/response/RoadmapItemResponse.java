package com.hackathon.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Roadmap item returned to clients.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapItemResponse {
    private Long id;
    private Integer weekNumber;
    private Long skillId;
    private String skillName;
    private String description;
}
