package com.rprelevic.xm.recom.api.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.function.Function;

import static com.rprelevic.xm.recom.utils.InstantUtils.toLocalDateTime;
import static com.rprelevic.xm.recom.utils.InstantUtils.toInstant;

public record SymbolProperties(String symbol, TimeWindow timeWindow, boolean locked, LocalDateTime lockedAt) {

    public enum TimeWindow {
        MONTHLY(endDate -> toInstant(toLocalDateTime(endDate).minusMonths(1).plusDays(1))),
        HALF_YEARLY(endDate -> toInstant(toLocalDateTime(endDate).minusMonths(6).plusDays(1))),
        YEARLY(endDate -> toInstant(toLocalDateTime(endDate).minusMonths(12).plusDays(1)));

        private final Function<Instant, Instant> findStart;

        TimeWindow(Function<Instant, Instant> findStart) {
            this.findStart = findStart;
        }

        public Instant findStart(Instant endDate) {
            return findStart.apply(endDate);
        }
    }
}
