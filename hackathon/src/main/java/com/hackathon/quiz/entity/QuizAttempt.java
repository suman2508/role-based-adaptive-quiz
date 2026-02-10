package com.hackathon.quiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "quiz_attempts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private QuizQuestion question;

    @Column(name = "selected_answer", length = 512)
    private String selectedAnswer;

    @Column(nullable = false)
    private Double score;

    @Column(name = "attempt_timestamp", nullable = false)
    private OffsetDateTime attemptTimestamp;
}
