package com.rprelevic.xm.recom.api.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.function.Function;

import static com.rprelevic.xm.recom.utils.InstantUtils.toLocalDateTime;
import static com.rprelevic.xm.recom.utils.InstantUtils.toInstant;

/**
 * Represents the properties of a symbol.
 */
public record SymbolProperties(String symbol, TimeWindow timeWindow, boolean locked, LocalDateTime lockedAt) {

    /**
     * Enum representing different time windows.
     */
    public enum TimeWindow {
        /**
         * Monthly time window.
         * Calculates the start date as one month before the end date, plus one day.
         */
        MONTHLY(endDate -> toInstant(toLocalDateTime(endDate).minusMonths(1).plusDays(1))),

        /**
         * Half-yearly time window.
         * Calculates the start date as six months before the end date, plus one day.
         */
        HALF_YEARLY(endDate -> toInstant(toLocalDateTime(endDate).minusMonths(6).plusDays(1))),

        /**
         * Yearly time window.
         * Calculates the start date as twelve months before the end date, plus one day.
         */
        YEARLY(endDate -> toInstant(toLocalDateTime(endDate).minusMonths(12).plusDays(1)));

        private final Function<Instant, Instant> findStart;

        /**
         * Constructor for the TimeWindow enum.
         *
         * @param findStart A function that calculates the start date based on the end date.
         */
        TimeWindow(Function<Instant, Instant> findStart) {
            this.findStart = findStart;
        }

        /**
         * Finds the start date based on the end date.
         *
         * @param endDate The end date.
         * @return The start date.
         */
        public Instant findStart(Instant endDate) {
            return findStart.apply(endDate);
        }
    }
}