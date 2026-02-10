package com.hackathon.quiz.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RoleAnalyzeResponse {
    private Long roleId;
    private String roleName;
    private List<SkillResponse> skills;
}
