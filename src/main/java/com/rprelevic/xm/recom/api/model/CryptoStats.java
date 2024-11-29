package com.rprelevic.xm.recom.api.model;

import java.time.LocalDateTime;

public record CryptoStats(LocalDateTime periodStart,
                          LocalDateTime periodEnd,
                          String symbol,
                          DataStatus status,
                          double minRate,
                          double maxRate,
                          double oldestRate,
                          double latestRate,
                          double normalizedRange) {

    public static CryptoStats red(String symbol, LocalDateTime periodStart, LocalDateTime periodEnd) {
        return new CryptoStats(periodStart, periodEnd, symbol, DataStatus.RED, 0, 0, 0, 0, 0);
    }

    public static CryptoStats of(String symbol, LocalDateTime periodStart, LocalDateTime periodEnd,
                                 DataStatus dataStatus, double minRate, double maxRate,
                                 double oldestRate, double latestRate, double normalizedRange) {
        return new CryptoStats(periodStart, periodEnd, symbol, dataStatus, minRate, maxRate, oldestRate, latestRate, normalizedRange);
    }
}