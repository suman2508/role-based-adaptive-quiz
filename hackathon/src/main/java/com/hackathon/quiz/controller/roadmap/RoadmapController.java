package com.hackathon.quiz.controller.roadmap;

import com.hackathon.quiz.dto.response.RoadmapItemResponse;
import com.hackathon.quiz.service.RoadmapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roadmap")
@RequiredArgsConstructor
@Tag(name = "Roadmap")
public class RoadmapController {

    private final RoadmapService roadmapService;

    @Operation(summary = "Generate a weekly roadmap for the user based on their target role")
    @PostMapping("/generate/{userId}")
    public ResponseEntity<List<RoadmapItemResponse>> generate(@PathVariable Long userId) {
        return ResponseEntity.ok(roadmapService.generateForUser(userId));
    }

    @Operation(summary = "Get the user's roadmap")
    @GetMapping("/{userId}")
    public ResponseEntity<List<RoadmapItemResponse>> get(@PathVariable Long userId) {
        return ResponseEntity.ok(roadmapService.getForUser(userId));
    }
}
