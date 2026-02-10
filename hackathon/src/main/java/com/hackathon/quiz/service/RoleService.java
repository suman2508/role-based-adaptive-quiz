package com.hackathon.quiz.service;

import com.hackathon.quiz.dto.request.RoleAnalyzeRequest;
import com.hackathon.quiz.dto.response.RoleAnalyzeResponse;
import com.hackathon.quiz.dto.response.SkillResponse;

import java.util.List;

public interface RoleService {
    RoleAnalyzeResponse analyzeRole(RoleAnalyzeRequest request);
    List<SkillResponse> getSkillsByRole(Long roleId);
}
