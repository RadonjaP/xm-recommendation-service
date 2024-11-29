package com.rprelevic.xm.recom.impl;

import com.rprelevic.xm.recom.api.model.ConsolidationResult;
import com.rprelevic.xm.recom.api.model.DataStatus;
import com.rprelevic.xm.recom.api.model.Rate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RatesConsolidatorImplTest {

    private RatesConsolidatorImpl ratesConsolidator;

    @BeforeEach
    void setUp() {
        ratesConsolidator = new RatesConsolidatorImpl();
    }

    @Test
    void givenNoConflictsAndCompleteData_whenConsolidate_thenStatusIsGreen() {
        List<Rate> existingRates = List.of(
                new Rate(Instant.parse("2023-01-01T00:00:00Z"), "USD", 1.0),
                new Rate(Instant.parse("2023-01-02T00:00:00Z"), "USD", 1.0)
        );
        List<Rate> newRates = List.of(
                new Rate(Instant.parse("2023-01-03T00:00:00Z"), "USD", 1.0)
        );
        Instant startPeriod = Instant.parse("2023-01-01T00:00:00Z");
        Instant endPeriod = Instant.parse("2023-01-03T00:00:00Z");

        ConsolidationResult result = ratesConsolidator.consolidate(existingRates, newRates, startPeriod, endPeriod);

        assertEquals(DataStatus.GREEN, result.dataStatus());
    }

    @Test
    void givenConflicts_whenConsolidate_thenStatusIsRed() {
        List<Rate> existingRates = List.of(
                new Rate(Instant.parse("2023-01-01T00:00:00Z"), "USD", 1.0)
        );
        List<Rate> newRates = List.of(
                new Rate(Instant.parse("2023-01-01T00:00:00Z"), "USD", 1.0)
        );
        Instant startPeriod = Instant.parse("2023-01-01T00:00:00Z");
        Instant endPeriod = Instant.parse("2023-01-02T00:00:00Z");

        ConsolidationResult result = ratesConsolidator.consolidate(existingRates, newRates, startPeriod, endPeriod);

        assertEquals(DataStatus.RED, result.dataStatus());
    }

    @Test
    void givenIncompleteData_whenConsolidate_thenStatusIsAmber() {
        List<Rate> existingRates = List.of(
                new Rate(Instant.parse("2023-01-01T00:00:00Z"), "USD", 1.0)
        );
        List<Rate> newRates = List.of(
                new Rate(Instant.parse("2023-01-03T00:00:00Z"), "USD", 1.0)
        );
        Instant startPeriod = Instant.parse("2023-01-01T00:00:00Z");
        Instant endPeriod = Instant.parse("2023-01-03T00:00:00Z");

        ConsolidationResult result = ratesConsolidator.consolidate(existingRates, newRates, startPeriod, endPeriod);

        assertEquals(DataStatus.AMBER, result.dataStatus());
    }

}