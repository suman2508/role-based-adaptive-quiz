package com.hackathon.quiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quiz_questions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Skill skill;

    // easy | medium | hard (validated at service layer)
    @Column(nullable = false, length = 16)
    private String difficulty;

    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    // Store options as JSON string for portability; service layer provides typed DTOs
    @Column(columnDefinition = "JSONB", nullable = false)
    private String options;

    @Column(name = "correct_answer", length = 512, nullable = false)
    private String correctAnswer;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    // Optional: embedding vector via later migration when pgvector enabled
    // @Column(name = "embedding_vector")
    // private float[] embeddingVector; // map via custom type when enabled
}
