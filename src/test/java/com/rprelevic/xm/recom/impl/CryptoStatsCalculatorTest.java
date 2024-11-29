package com.rprelevic.xm.recom.impl;

import com.rprelevic.xm.recom.api.CryptoStatsCalculator;
import com.rprelevic.xm.recom.api.model.ConsolidationResult;
import com.rprelevic.xm.recom.api.model.CryptoStats;
import com.rprelevic.xm.recom.api.model.DataStatus;
import com.rprelevic.xm.recom.api.model.Rate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CryptoStatsCalculatorTest {

    private CryptoStatsCalculator cryptoStatsCalculator;

    @BeforeEach
    void setUp() {
        cryptoStatsCalculator = new CryptoStatsCalculatorImpl();
    }


    @Test
    void calculateStats_withValidRates_returnsCorrectStats() {
        // given
        ConsolidationResult result = new ConsolidationResult(
                List.of(
                        new Rate(Instant.parse("2023-09-01T00:00:00Z"), "BTC", 100.0),
                        new Rate(Instant.parse("2023-09-15T00:00:00Z"), "BTC", 200.0),
                        new Rate(Instant.parse("2023-09-30T00:00:00Z"), "BTC", 150.0)
                ),
                DataStatus.GREEN,
                Instant.parse("2023-09-01T00:00:00Z"),
                Instant.parse("2023-10-01T00:00:00Z"),
                "BTC"
        );

        // when
        CryptoStats stats = cryptoStatsCalculator.calculateStats(result);

        // then
        assertEquals("BTC", stats.symbol());
        assertEquals(100.0, stats.minRate());
        assertEquals(200.0, stats.maxRate());
        assertEquals(100.0, stats.oldestRate());
        assertEquals(150.0, stats.latestRate());
        assertEquals(1.0, stats.normalizedRange());
    }

    @Test
    void calculateStats_withRedDataStatus_returnsRedStats() {
        // given
        ConsolidationResult result = new ConsolidationResult(
                List.of(),
                DataStatus.RED,
                Instant.parse("2023-09-01T00:00:00Z"),
                Instant.parse("2023-10-01T00:00:00Z"),
                "BTC"
        );

        // when
        CryptoStats stats = cryptoStatsCalculator.calculateStats(result);

        // then
        assertEquals("BTC", stats.symbol());
        assertEquals(DataStatus.RED, stats.status());
    }

}
