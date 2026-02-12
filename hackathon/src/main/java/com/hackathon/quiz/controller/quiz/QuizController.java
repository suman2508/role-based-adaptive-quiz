package com.hackathon.quiz.controller.quiz;

import com.hackathon.quiz.dto.request.QuizStartRequest;
import com.hackathon.quiz.dto.request.QuizSubmitRequest;
import com.hackathon.quiz.dto.response.QuizQuestionResponse;
import com.hackathon.quiz.dto.response.QuizSubmitResponse;
import com.hackathon.quiz.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quiz")
@RequiredArgsConstructor
@Tag(name = "Quiz")
public class QuizController {

    private final QuizService quizService;

    @Operation(summary = "Start a quiz session (returns a set of questions)")
    @PostMapping("/start")
    public ResponseEntity<List<QuizQuestionResponse>> start(@Valid @RequestBody QuizStartRequest request) {
        return ResponseEntity.ok(quizService.start(request));
    }

    @Operation(summary = "Submit an answer for a question")
    @PostMapping("/submit")
    public ResponseEntity<QuizSubmitResponse> submit(@Valid @RequestBody QuizSubmitRequest request) {
        return ResponseEntity.ok(quizService.submit(request));
    }

    @Operation(summary = "Get next adaptive question for a user")
    @GetMapping("/next-adaptive")
    public ResponseEntity<QuizQuestionResponse> nextAdaptive(@RequestParam("userId") Long userId) {
        return ResponseEntity.ok(quizService.nextAdaptive(userId));
    }
}
