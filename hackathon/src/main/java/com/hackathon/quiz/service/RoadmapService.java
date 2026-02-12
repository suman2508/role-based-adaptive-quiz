package com.hackathon.quiz.service;

import com.hackathon.quiz.dto.response.RoadmapItemResponse;

import java.util.List;

public interface RoadmapService {
    List<RoadmapItemResponse> generateForUser(Long userId);
    List<RoadmapItemResponse> getForUser(Long userId);
}
