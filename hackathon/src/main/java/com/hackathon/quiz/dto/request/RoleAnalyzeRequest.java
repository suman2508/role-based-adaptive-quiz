package com.hackathon.quiz.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleAnalyzeRequest {
    @NotBlank
    @Size(max = 200)
    private String roleName;

    // Optional: limit number of top skills to persist/return
    private Integer topN;
}
