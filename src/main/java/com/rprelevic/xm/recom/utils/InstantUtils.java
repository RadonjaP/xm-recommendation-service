package com.rprelevic.xm.recom.utils;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Utility class for converting between different date and time representations.
 */
public final class InstantUtils {

    /**
     * Prevent instantiation of this utility class.
     */
    private InstantUtils() {
        throw new UnsupportedOperationException("InstantUtils is a utility class and should not be instantiated");
    }

    /**
     * Converts a timestamp in milliseconds to an Instant.
     *
     * @param timestamp the timestamp in milliseconds
     * @return the corresponding Instant
     */
    public static Instant toInstant(long timestamp) {
        return Instant.ofEpochMilli(timestamp);
    }

    /**
     * Converts a LocalDateTime to an Instant.
     *
     * @param localDateTime the LocalDateTime to convert
     * @return the corresponding Instant
     */
    public static Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.UTC);
    }

    /**
     * Converts a SQL Timestamp to an Instant.
     *
     * @param dateTime the Timestamp to convert
     * @return the corresponding Instant
     */
    public static Instant toInstant(Timestamp dateTime) {
        return dateTime.toInstant();
    }

    /**
     * Converts an Instant to a LocalDateTime.
     *
     * @param instant the Instant to convert
     * @return the corresponding LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    /**
     * Converts an Instant to a LocalDate.
     *
     * @param instant the Instant to convert
     * @return the corresponding LocalDate
     */
    public static LocalDate toLocalDate(Instant instant) {
        return LocalDate.ofInstant(instant, ZoneOffset.UTC);
    }

}