package com.rprelevic.xm.recom.impl;

import com.rprelevic.xm.recom.api.model.Rate;

import java.time.Instant;

/**
 * Accumulates statistics about rates.
 */
public class RateStatsAccumulator {

    private double minRate;
    private double maxRate;
    private double oldestRate;
    private double latestRate;
    private Instant oldestDateTime;
    private Instant latestDateTime;

    private RateStatsAccumulator() {
        this.minRate = Double.MAX_VALUE;;
        this.maxRate = Double.MIN_VALUE;;
        this.oldestRate = 0.0;
        this.latestRate = 0.0;
        this.oldestDateTime = Instant.MAX;
        this.latestDateTime = Instant.MIN;
    }

    /**
     * Accumulates statistics about the given rate.
     *
     * @param rate the rate to accumulate statistics about
     */
    public void accumulate(Rate rate) {
        double currentRate = rate.rate();
        Instant currentDateTime = rate.dateTime();

        if (currentRate < minRate) {
            minRate = currentRate;
        }
        if (currentRate > maxRate) {
            maxRate = currentRate;
        }
        if (currentDateTime.isBefore(oldestDateTime)) {
            oldestDateTime = currentDateTime;
            oldestRate = currentRate;
        }
        if (currentDateTime.isAfter(latestDateTime)) {
            latestDateTime = currentDateTime;
            latestRate = currentRate;
        }
    }

    public double minRate() {
        return minRate;
    }

    public double maxRate() {
        return maxRate;
    }

    public double oldestRate() {
        return oldestRate;
    }

    public double latestRate() {
        return latestRate;
    }

    /**
     * Static factory method to create a new RateStatsAccumulator.
     *
     * @return a new RateStatsAccumulator
     */
    public static RateStatsAccumulator create() {
        return new RateStatsAccumulator();
    }

}
