package com.hackathon.quiz.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Registration payload for creating a new user account.
 */
@Data
public class RegisterRequest {

    @NotBlank
    @Size(min = 2, max = 200)
    private String name;

    @NotBlank
    @Email
    @Size(max = 320)
    private String email;

    @NotBlank
    @Size(min = 8, max = 72) // BCrypt safe range
    private String password;

    // Optional: initial target role the user wants to prepare for
    @Size(max = 200)
    private String targetRole;
}
