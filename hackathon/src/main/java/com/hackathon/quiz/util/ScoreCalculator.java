package com.hackathon.quiz.util;

import java.util.List;

/**
 * Scoring utilities for quiz attempts and readiness computation.
 * Keep pure/stateless for easy unit testing.
 */
public final class ScoreCalculator {
    private ScoreCalculator() {}

    /**
     * Returns 1.0 if selected equals correct (case-sensitive by default), else 0.0.
     */
    public static double correctness(String selected, String correct) {
        if (selected == null || correct == null) return 0.0;
        return selected.equals(correct) ? 1.0 : 0.0;
    }

    /**
     * Case-insensitive correctness.
     */
    public static double correctnessIgnoreCase(String selected, String correct) {
        if (selected == null || correct == null) return 0.0;
        return selected.equalsIgnoreCase(correct) ? 1.0 : 0.0;
    }

    /**
     * Normalized score in [0,1] from raw points and max points.
     */
    public static double normalize(int points, int maxPoints) {
        if (maxPoints <= 0) return 0.0;
        if (points <= 0) return 0.0;
        if (points >= maxPoints) return 1.0;
        return (double) points / (double) maxPoints;
    }

    /**
     * Simple moving average (SMA) over last N values. If values < N, averages what is present.
     */
    public static double movingAverage(List<Double> values, int window) {
        if (values == null || values.isEmpty() || window <= 0) return 0.0;
        int size = values.size();
        int from = Math.max(0, size - window);
        double sum = 0.0;
        for (int i = from; i < size; i++) {
            Double v = values.get(i);
            sum += v != null ? v : 0.0;
        }
        return sum / (size - from);
    }

    /**
     * Weighted average where weights[i] applies to values[i]. If size mismatch, up to min size used.
     */
    public static double weightedAverage(List<Double> values, List<Double> weights) {
        if (values == null || weights == null) return 0.0;
        int n = Math.min(values.size(), weights.size());
        if (n == 0) return 0.0;
        double wsum = 0.0;
        double vsum = 0.0;
        for (int i = 0; i < n; i++) {
            double v = values.get(i) != null ? values.get(i) : 0.0;
            double w = weights.get(i) != null ? weights.get(i) : 0.0;
            vsum += v * w;
            wsum += w;
        }
        if (wsum == 0.0) return 0.0;
        return vsum / wsum;
    }

    /**
     * Readiness score heuristic:
     * - coverageRatio in [0,1] (skills attempted / required skills)
     * - performance in [0,1] (average correctness across attempts)
     * - recencyBoost in [0,1] (more recent successful attempts)
     * Combines as: 0.5*performance + 0.3*coverage + 0.2*recency, clamped to [0,1].
     */
    public static double readinessScore(double coverageRatio, double performance, double recencyBoost) {
        double score = 0.5 * clamp01(performance) + 0.3 * clamp01(coverageRatio) + 0.2 * clamp01(recencyBoost);
        return clamp01(score);
    }

    /**
     * Converts [0,1] normalized score to percentage [0,100].
     */
    public static int toPercent(double normalized) {
        double v = clamp01(normalized) * 100.0;
        return (int) Math.round(v);
    }

    private static double clamp01(double v) {
        if (v < 0.0) return 0.0;
        if (v > 1.0) return 1.0;
        return v;
    }
}
