package com.rprelevic.xm.recom.api.model;

import java.time.LocalDateTime;

public record SymbolProperties(String symbol, TimeWindow timeWindow, boolean locked, LocalDateTime lockedAt) {

    public enum TimeWindow {
        MONTHLY,
        HALF_YEARLY,
        YEARLY
    }
}
