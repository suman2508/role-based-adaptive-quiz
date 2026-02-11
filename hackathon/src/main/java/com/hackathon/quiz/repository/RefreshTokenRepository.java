package com.hackathon.quiz.repository;

import com.hackathon.quiz.entity.RefreshToken;
import com.hackathon.quiz.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    long deleteByUser(User user);
    long deleteByExpiresAtBefore(OffsetDateTime cutoff);
}
