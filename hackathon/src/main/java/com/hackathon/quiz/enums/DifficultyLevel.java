package com.hackathon.quiz.enums;

/**
 * Difficulty graduated in three bands for adaptive progression.
 */
public enum DifficultyLevel {
    EASY,
    MEDIUM,
    HARD;

    public boolean harderThan(DifficultyLevel other) {
        return this.ordinal() > other.ordinal();
    }

    public boolean easierThan(DifficultyLevel other) {
        return this.ordinal() < other.ordinal();
    }
}
