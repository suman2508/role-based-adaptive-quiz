package com.hackathon.quiz.service.impl;

import com.hackathon.quiz.dto.auth.AuthResponse;
import com.hackathon.quiz.dto.auth.LoginRequest;
import com.hackathon.quiz.dto.auth.RefreshRequest;
import com.hackathon.quiz.dto.auth.RegisterRequest;
import com.hackathon.quiz.entity.RefreshToken;
import com.hackathon.quiz.entity.User;
import com.hackathon.quiz.enums.UserRole;
import com.hackathon.quiz.exception.BusinessException;
import com.hackathon.quiz.exception.ValidationException;
import com.hackathon.quiz.repository.RefreshTokenRepository;
import com.hackathon.quiz.repository.UserRepository;
import com.hackathon.quiz.security.JwtService;
import com.hackathon.quiz.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        validateRegister(request);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail().toLowerCase())
                .targetRole(request.getTargetRole())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .readinessScore(0.0)
                .build();

        user = userRepository.save(user);

        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);

        persistRefreshToken(user, refresh);

        return AuthResponse.builder()
                .tokenType("Bearer")
                .accessToken(access)
                .refreshToken(refresh)
                .expiresIn(jwtService.getExpirationMs())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        validateLogin(request);

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new BusinessException("Invalid credentials"));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Invalid credentials");
        }

        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);

        // Invalidate existing refresh tokens for this user (optional strategy - rotation)
        refreshTokenRepository.deleteByUser(user);
        persistRefreshToken(user, refresh);

        return AuthResponse.builder()
                .tokenType("Bearer")
                .accessToken(access)
                .refreshToken(refresh)
                .expiresIn(jwtService.getExpirationMs())
                .build();
    }

    @Override
    public AuthResponse refresh(RefreshRequest request) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            throw new ValidationException("Refresh token is required");
        }

        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new BusinessException("Refresh token expired or revoked");
        }

        // Validate JWT subject matches stored user
        String username = jwtService.extractUsername(request.getRefreshToken());
        if (username == null || !Objects.equals(username.toLowerCase(), stored.getUser().getEmail().toLowerCase())) {
            throw new BusinessException("Refresh token subject mismatch");
        }

        // Rotate refresh token
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        User user = stored.getUser();
        String newAccess = jwtService.generateAccessToken(user);
        String newRefresh = jwtService.generateRefreshToken(user);
        persistRefreshToken(user, newRefresh);

        return AuthResponse.builder()
                .tokenType("Bearer")
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .expiresIn(jwtService.getExpirationMs())
                .build();
    }

    private void validateRegister(RegisterRequest r) {
        if (r == null) throw new ValidationException("Request is required");
        if (isBlank(r.getName())) throw new ValidationException("Name is required");
        if (isBlank(r.getEmail())) throw new ValidationException("Email is required");
        if (isBlank(r.getPassword())) throw new ValidationException("Password is required");
        if (r.getPassword().length() < 8) throw new ValidationException("Password must be at least 8 characters");
    }

    private void validateLogin(LoginRequest r) {
        if (r == null) throw new ValidationException("Request is required");
        if (isBlank(r.getEmail()) || isBlank(r.getPassword())) throw new ValidationException("Email and password are required");
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void persistRefreshToken(User user, String refreshToken) {
        OffsetDateTime expiresAt = OffsetDateTime.now().plusSeconds(Math.max(jwtService.getExpirationMs() / 1000 * 7, 86400)); // ~7x access or min 1 day
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();
        refreshTokenRepository.save(token);
    }
}
