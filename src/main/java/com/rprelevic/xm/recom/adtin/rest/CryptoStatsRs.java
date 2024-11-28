package com.rprelevic.xm.recom.adtin.rest;

/**
 * Response object for CryptoStats.
 *
 * @param symbol     the symbol of the cryptocurrency
 * @param minRate    the minimum rate of the cryptocurrency
 * @param maxRate    the maximum rate of the cryptocurrency
 * @param oldestRate the oldest rate of the cryptocurrency
 * @param latestRate the latest rate of the cryptocurrency
 */
public record CryptoStatsRs(String symbol,
                            double minRate,
                            double maxRate,
                            double oldestRate,
                            double latestRate) {

    /**
     * Converts a CryptoStats object to a CryptoStatsRs object.
     *
     * @param cryptoStats the CryptoStats object to convert
     * @return the converted CryptoStatsRs object
     */
    public static CryptoStatsRs fromCryptoStats(com.rprelevic.xm.recom.api.model.CryptoStats cryptoStats) {
        return new CryptoStatsRs(cryptoStats.symbol(),
                cryptoStats.minRate(),
                cryptoStats.maxRate(),
                cryptoStats.oldestRate(),
                cryptoStats.latestRate());
    }

}
