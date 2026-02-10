package com.hackathon.quiz.controller.role;

import com.hackathon.quiz.dto.request.RoleAnalyzeRequest;
import com.hackathon.quiz.dto.response.RoleAnalyzeResponse;
import com.hackathon.quiz.dto.response.SkillResponse;
import com.hackathon.quiz.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping("/analyze")
    public ResponseEntity<RoleAnalyzeResponse> analyze(@Valid @RequestBody RoleAnalyzeRequest request) {
        return ResponseEntity.ok(roleService.analyzeRole(request));
    }

    @GetMapping("/{roleId}/skills")
    public ResponseEntity<List<SkillResponse>> getSkills(@PathVariable Long roleId) {
        return ResponseEntity.ok(roleService.getSkillsByRole(roleId));
    }
}
