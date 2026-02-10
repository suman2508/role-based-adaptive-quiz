package com.hackathon.quiz.util;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

/**
 * Safe helpers for parsing/handling enums without sprinkling try/catch or valueOf calls.
 */
public final class EnumUtils {
    private EnumUtils() {}

    /**
     * Case-insensitive enum parse with fallback.
     */
    public static <E extends Enum<E>> E parseOrDefault(Class<E> enumType, String value, E defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Enum.valueOf(enumType, value.trim().replace(' ', '_').toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return defaultValue;
        }
    }

    /**
     * Case-insensitive enum parse returning Optional.
     */
    public static <E extends Enum<E>> Optional<E> parseOptional(Class<E> enumType, String value) {
        if (value == null || value.isBlank()) return Optional.empty();
        try {
            return Optional.of(Enum.valueOf(enumType, value.trim().replace(' ', '_').toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    /**
     * Maps an enum through a function if non-null, otherwise returns null.
     */
    public static <E extends Enum<E>, T> T mapOrNull(E e, Function<E, T> mapper) {
        return e == null ? null : mapper.apply(e);
    }
}
