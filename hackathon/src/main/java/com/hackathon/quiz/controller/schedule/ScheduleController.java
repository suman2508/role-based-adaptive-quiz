package com.hackathon.quiz.controller.schedule;

import com.hackathon.quiz.dto.response.PracticeScheduleResponse;
import com.hackathon.quiz.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
@Tag(name = "Practice Schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Operation(summary = "Get/generate daily practice schedule for a user (optionally within a date range)")
    @GetMapping("/{userId}")
    public ResponseEntity<List<PracticeScheduleResponse>> getSchedule(
            @PathVariable Long userId,
            @RequestParam(value = "start", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(value = "end", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ResponseEntity.ok(scheduleService.getSchedule(userId, start, end));
    }
}
