package com.hackathon.quiz.util;

import com.hackathon.quiz.enums.DifficultyLevel;

import java.util.Locale;
import java.util.Objects;

/**
 * Utility for converting difficulty representations and deriving difficulty from performance.
 */
public final class DifficultyMapper {
    private DifficultyMapper() {}

    /**
     * Maps case-insensitive string ("easy" / "EASY") to DifficultyLevel. Defaults to MEDIUM.
     */
    public static DifficultyLevel fromString(String value) {
        if (value == null || value.isBlank()) return DifficultyLevel.MEDIUM;
        String norm = value.trim().toUpperCase(Locale.ROOT);
        return switch (norm) {
            case "EASY" -> DifficultyLevel.EASY;
            case "HARD" -> DifficultyLevel.HARD;
            default -> DifficultyLevel.MEDIUM;
        };
    }

    /**
     * Converts DifficultyLevel to a lowercase string value to store in DB columns if needed.
     */
    public static String toStorageString(DifficultyLevel level) {
        Objects.requireNonNull(level, "difficulty level");
        return switch (level) {
            case EASY -> "easy";
            case MEDIUM -> "medium";
            case HARD -> "hard";
        };
    }

    /**
     * Simple heuristic: derive next difficulty from lastScore (0..1).
     * - score >= 0.8 -> HARD
     * - score >= 0.5 -> MEDIUM
     * - else -> EASY
     */
    public static DifficultyLevel fromScore(double lastScore) {
        if (lastScore >= 0.8) return DifficultyLevel.HARD;
        if (lastScore >= 0.5) return DifficultyLevel.MEDIUM;
        return DifficultyLevel.EASY;
    }

    /**
     * Progression rule: returns the next harder level, capped at HARD.
     */
    public static DifficultyLevel harder(DifficultyLevel current) {
        if (current == null) return DifficultyLevel.MEDIUM;
        return switch (current) {
            case EASY -> DifficultyLevel.MEDIUM;
            case MEDIUM -> DifficultyLevel.HARD;
            case HARD -> DifficultyLevel.HARD;
        };
    }

    /**
     * Regression rule: returns the next easier level, floored at EASY.
     */
    public static DifficultyLevel easier(DifficultyLevel current) {
        if (current == null) return DifficultyLevel.MEDIUM;
        return switch (current) {
            case HARD -> DifficultyLevel.MEDIUM;
            case MEDIUM -> DifficultyLevel.EASY;
            case EASY -> DifficultyLevel.EASY;
        };
    }
}
