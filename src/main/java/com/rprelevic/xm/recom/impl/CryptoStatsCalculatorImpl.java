package com.rprelevic.xm.recom.impl;

import com.rprelevic.xm.recom.api.CryptoStatsCalculator;
import com.rprelevic.xm.recom.api.model.ConsolidationResult;
import com.rprelevic.xm.recom.api.model.CryptoStats;
import com.rprelevic.xm.recom.api.model.Rate;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

import static com.rprelevic.xm.recom.api.model.DataStatus.RED;

public class CryptoStatsCalculatorImpl implements CryptoStatsCalculator {

    // TODO: Has to be fixed, use for loop to get stats
    // TODO: Take into consideration that every day has multiple rates
    @Override
    public CryptoStats calculateStats(ConsolidationResult result) {

        final var periodEndLocalDateTime = result.periodEnd().atZone(ZoneId.systemDefault()).toLocalDateTime();
        final var periodStartLocalDateTime = result.periodStart().atZone(ZoneId.systemDefault()).toLocalDateTime();

        if (RED.equals(result.dataStatus())) {
            return CryptoStats.red(result.symbol(), periodStartLocalDateTime, periodEndLocalDateTime);
        }

        // TODO: Calculate min, max, oldest, latest, normalized range
        List<Rate> rates = result.consolidatedRates();
        double minRate = rates.stream().mapToDouble(Rate::rate).min().orElse(0);
        double maxRate = rates.stream().mapToDouble(Rate::rate).max().orElse(0);
        double oldestRate = rates.stream().min(Comparator.comparing(Rate::dateTime)).map(Rate::rate).orElse(0.0);
        double latestRate = rates.stream().max(Comparator.comparing(Rate::dateTime)).map(Rate::rate).orElse(0.0);
        double normalizedRange = (maxRate - minRate) / minRate;

        return CryptoStats.of(result.symbol(), periodStartLocalDateTime, periodEndLocalDateTime,
                result.dataStatus(), minRate, maxRate, oldestRate, latestRate, normalizedRange);
    }
}
