package com.hackathon.quiz.service;

import com.hackathon.quiz.dto.auth.AuthResponse;
import com.hackathon.quiz.dto.auth.LoginRequest;
import com.hackathon.quiz.dto.auth.RefreshRequest;
import com.hackathon.quiz.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(RefreshRequest request);
}
