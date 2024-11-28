package com.rprelevic.xm.recom.impl;

import com.rprelevic.xm.recom.api.PriceConsolidator;
import com.rprelevic.xm.recom.api.model.ConsolidationResult;
import com.rprelevic.xm.recom.api.model.DataStatus;
import com.rprelevic.xm.recom.api.model.Price;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PriceConsolidatorImpl implements PriceConsolidator {

    @Override
    public ConsolidationResult consolidate(List<Price> existingPrices, List<Price> newPrices, Instant startPeriod, Instant endPeriod) {

        final var symbol = newPrices.get(0).symbol();

        // If subset of new and existing prices is not empty
        final var existingPriceSet = new HashSet<>(existingPrices);
        for (Price newPrice : newPrices) {
            if (existingPriceSet.contains(newPrice)) {
                return new ConsolidationResult(null, DataStatus.RED, startPeriod, endPeriod, symbol);
            } else {
                existingPriceSet.add(newPrice);
            }
        }

        final var allDays = findAllDaysBetween(startPeriod, endPeriod);

        // Verify that all days are present in the existing prices
        final Set<Instant> existingPriceDates = existingPrices.stream()
                .map(Price::dateTime)
                .collect(Collectors.toSet());

        for (Instant day : allDays) {
            if (!existingPriceDates.contains(day)) {
                return new ConsolidationResult(existingPriceSet.stream().toList(), DataStatus.AMBER, startPeriod, endPeriod, symbol);
            }
        }

        return new ConsolidationResult(existingPriceSet.stream().toList(), DataStatus.GREEN, startPeriod, endPeriod, symbol);
    }

    private Set<Instant> findAllDaysBetween(Instant start, Instant end) {
        LocalDate startDate = start.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = end.atZone(ZoneId.systemDefault()).toLocalDate();

        return startDate.datesUntil(endDate.plusDays(1))
                .map(localDate -> localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                .collect(Collectors.toSet());
    }

}
