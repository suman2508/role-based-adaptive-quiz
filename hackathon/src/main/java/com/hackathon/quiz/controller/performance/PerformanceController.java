package com.hackathon.quiz.controller.performance;

import com.hackathon.quiz.dto.response.PerformanceResponse;
import com.hackathon.quiz.service.PerformanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/performance")
@RequiredArgsConstructor
@Tag(name = "Performance & Readiness")
public class PerformanceController {

    private final PerformanceService performanceService;

    @Operation(summary = "Get aggregated performance and readiness details for a user")
    @GetMapping("/{userId}")
    public ResponseEntity<PerformanceResponse> get(@PathVariable Long userId) {
        return ResponseEntity.ok(performanceService.getPerformance(userId));
    }

    @Operation(summary = "Get readiness score (0..1) for a user")
    @GetMapping("/readiness-score/{userId}")
    public ResponseEntity<Double> readiness(@PathVariable Long userId) {
        return ResponseEntity.ok(performanceService.getReadinessScore(userId));
    }
}
