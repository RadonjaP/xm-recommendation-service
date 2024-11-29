package com.rprelevic.xm.recom.impl;

import com.rprelevic.xm.recom.api.RatesConsolidator;
import com.rprelevic.xm.recom.api.model.ConsolidationResult;
import com.rprelevic.xm.recom.api.model.DataStatus;
import com.rprelevic.xm.recom.api.model.Rate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RatesConsolidatorImpl implements RatesConsolidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RatesConsolidatorImpl.class);

    @Override
    public ConsolidationResult consolidate(List<Rate> existingRates, List<Rate> newRates, Instant startPeriod, Instant endPeriod) {

        final var symbol = newRates.get(0).symbol();

        // If subset of new and existing rates is not empty
        final var existingRatesSet = new HashSet<>(existingRates);
        for (Rate newRate : newRates) {
            if (existingRatesSet.contains(newRate)) {
                LOGGER.warn("Unresolved conflicts. Rate already exists for timestamp {}, symbol {}. Status for period is turning RED", newRate.dateTime(), symbol);
                return new ConsolidationResult(null, DataStatus.RED, startPeriod, endPeriod, symbol);
            } else {
                existingRatesSet.add(newRate);
            }
        }

        final var datesInPeriod = findAllDaysBetween(startPeriod, endPeriod);

        // Verify that all days are present in the existing rates
        final Set<LocalDate> existingRatesDates = existingRatesSet.stream()
                .map(rate -> rate.dateTime().atZone(ZoneId.systemDefault()).toLocalDate())
                .collect(Collectors.toSet());

        if (existingRatesDates.containsAll(datesInPeriod)) {
            return new ConsolidationResult(existingRatesSet.stream().toList(), DataStatus.GREEN, startPeriod, endPeriod, symbol);
        } else {
            LOGGER.warn("Data is incomplete. Missing rates for period {} - {}. Status for period is turning AMBER", startPeriod, endPeriod);
            return new ConsolidationResult(existingRatesSet.stream().toList(), DataStatus.AMBER, startPeriod, endPeriod, symbol);
        }
    }

    private Set<LocalDate> findAllDaysBetween(Instant start, Instant end) {
        final var startDate = start.atZone(ZoneId.systemDefault()).toLocalDate();
        final var endDate = end.atZone(ZoneId.systemDefault()).toLocalDate();

        return startDate.datesUntil(endDate.plusDays(1))
                .collect(Collectors.toSet());
    }

}