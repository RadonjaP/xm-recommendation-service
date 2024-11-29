package com.rprelevic.xm.recom.impl;

import com.rprelevic.xm.recom.api.CryptoStatsCalculator;
import com.rprelevic.xm.recom.api.model.ConsolidationResult;
import com.rprelevic.xm.recom.api.model.CryptoStats;

import static com.rprelevic.xm.recom.api.model.DataStatus.RED;
import static com.rprelevic.xm.recom.utils.InstantUtils.toLocalDateTime;

/**
 * Implementation of the {@link CryptoStatsCalculator} interface.
 */
public class CryptoStatsCalculatorImpl implements CryptoStatsCalculator {

    @Override
    public CryptoStats calculateStats(ConsolidationResult result) {

        final var periodEnd = toLocalDateTime(result.periodEnd());
        final var periodStart = toLocalDateTime(result.periodStart());

        if (RED.equals(result.dataStatus())) {
            return CryptoStats.red(result.symbol(), periodStart, periodEnd);
        }

        final var rates = result.consolidatedRates();

        final var accumulator = RateStatsAccumulator.create();
        rates.forEach(accumulator::accumulate);

        double normalizedRange = (accumulator.maxRate() - accumulator.minRate())
                / accumulator.minRate();

        return CryptoStats.of(result.symbol(), periodStart, periodEnd,
                result.dataStatus(), accumulator.minRate(), accumulator.maxRate(),
                accumulator.oldestRate(), accumulator.latestRate(), normalizedRange);
    }

}
