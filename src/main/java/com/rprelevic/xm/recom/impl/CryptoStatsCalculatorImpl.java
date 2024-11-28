package com.rprelevic.xm.recom.impl;

import com.rprelevic.xm.recom.api.CryptoStatsCalculator;
import com.rprelevic.xm.recom.api.model.ConsolidationResult;
import com.rprelevic.xm.recom.api.model.CryptoStats;
import com.rprelevic.xm.recom.api.model.Price;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

import static com.rprelevic.xm.recom.api.model.DataStatus.RED;

public class CryptoStatsCalculatorImpl implements CryptoStatsCalculator {

    // TODO: Has to be fixed, use for loop to get stats
    // TODO: Take into consideration that every day has multiple prices
    @Override
    public CryptoStats calculateStats(ConsolidationResult result) {

        final var periodEndLocalDateTime = result.periodEnd().atZone(ZoneId.systemDefault()).toLocalDateTime();
        final var periodStartLocalDateTime = result.periodStart().atZone(ZoneId.systemDefault()).toLocalDateTime();

        if (RED.equals(result.dataStatus())) {
            return CryptoStats.red(result.symbol(), periodStartLocalDateTime, periodEndLocalDateTime);
        }

        // TODO: Calculate min, max, oldest, latest, normalized range
        List<Price> prices = result.consolidatedPrices();
        double minPrice = prices.stream().mapToDouble(Price::rate).min().orElse(0);
        double maxPrice = prices.stream().mapToDouble(Price::rate).max().orElse(0);
        double oldestPrice = prices.stream().min(Comparator.comparing(Price::dateTime)).map(Price::rate).orElse(0.0);
        double latestPrice = prices.stream().max(Comparator.comparing(Price::dateTime)).map(Price::rate).orElse(0.0);
        double normalizedRange = (maxPrice - minPrice) / minPrice;

        return CryptoStats.of(result.symbol(), periodStartLocalDateTime, periodEndLocalDateTime,
                result.dataStatus(), minPrice, maxPrice, oldestPrice, latestPrice, normalizedRange);
    }
}
