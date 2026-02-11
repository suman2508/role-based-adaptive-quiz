package com.hackathon.quiz.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Refresh token request payload.
 */
@Data
public class RefreshRequest {
    @NotBlank
    private String refreshToken;
}
