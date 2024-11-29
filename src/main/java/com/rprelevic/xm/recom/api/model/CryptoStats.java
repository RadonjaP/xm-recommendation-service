package com.rprelevic.xm.recom.api.model;

import java.time.LocalDateTime;

/**
 * Represents the statistics for a cryptocurrency.
 */
public record CryptoStats(LocalDateTime periodStart,
                          LocalDateTime periodEnd,
                          String symbol,
                          DataStatus status,
                          double minRate,
                          double maxRate,
                          double oldestRate,
                          double latestRate,
                          double normalizedRange) {

    /**
     * Creates a new CryptoStats entity with the given symbol, period start and end, and status set to RED.
     *
     * @param symbol      the symbol of the cryptocurrency
     * @param periodStart the start of the period
     * @param periodEnd   the end of the period
     * @return a new CryptoStats entity with the given symbol, period start and end, and status set to {@link DataStatus#RED}
     */
    public static CryptoStats red(String symbol, LocalDateTime periodStart, LocalDateTime periodEnd) {
        return new CryptoStats(periodStart, periodEnd, symbol, DataStatus.RED, 0, 0, 0, 0, 0);
    }

    /**
     * Creates a new CryptoStats entity with the given symbol, period start and end, and status set to AMBER or GREEN.
     *
     * @param symbol      the symbol of the cryptocurrency
     * @param periodStart the start of the period
     * @param periodEnd   the end of the period
     * @param dataStatus  the status of the data, either {@link DataStatus#AMBER} or {@link DataStatus#GREEN}
     * @param minRate     the minimum rate during the period
     * @param maxRate     the maximum rate during the period
     * @param oldestRate  the oldest rate during the period
     * @param latestRate  the latest rate during the period
     * @param normalizedRange the normalized range during the period
     * @return a new CryptoStats entity with the given symbol, period start and end, and proper status
     */
    public static CryptoStats of(String symbol, LocalDateTime periodStart, LocalDateTime periodEnd,
                                 DataStatus dataStatus, double minRate, double maxRate,
                                 double oldestRate, double latestRate, double normalizedRange) {
        return new CryptoStats(periodStart, periodEnd, symbol, dataStatus, minRate, maxRate, oldestRate, latestRate, normalizedRange);
    }
}