package com.hackathon.quiz.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Date/time helpers standardized to UTC where applicable.
 */
public final class DateUtils {
    private DateUtils() {}

    public static final ZoneId UTC = ZoneId.of("UTC");
    public static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_DATE; // yyyy-MM-dd
    public static final DateTimeFormatter ISO_OFFSET_DT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public static LocalDate nowDateUtc() {
        return LocalDate.now(UTC);
    }

    public static OffsetDateTime nowOffsetUtc() {
        return OffsetDateTime.now(UTC);
    }

    public static LocalDate toLocalDateUtc(Instant instant) {
        Objects.requireNonNull(instant, "instant");
        return instant.atZone(UTC).toLocalDate();
    }

    public static OffsetDateTime toOffsetUtc(Instant instant) {
        Objects.requireNonNull(instant, "instant");
        return instant.atOffset(ZoneOffset.UTC);
    }

    public static String formatIsoDate(LocalDate date) {
        return date == null ? null : ISO_DATE.format(date);
    }

    public static String formatIsoOffset(OffsetDateTime odt) {
        return odt == null ? null : ISO_OFFSET_DT.format(odt);
    }
}
