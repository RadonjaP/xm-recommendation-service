package com.rprelevic.xm.recom.api;

import com.rprelevic.xm.recom.api.model.ConsolidationResult;
import com.rprelevic.xm.recom.api.model.Rate;

import java.time.Instant;
import java.util.List;

/**
 * Interface for consolidating rates.
 */
public interface RatesConsolidator {

    /**
     * Consolidates the given rates. The consolidation is done by merging the existing rates with the new rates.
     * If new rates contain rates for the same period as the existing rates, ingestion will be stopped and status of data
     * will be set to {@link com.rprelevic.xm.recom.api.model.DataStatus#RED}.
     * <p>
     * This is in order to prevent data corruption. In the future we can consider having a more sophisticated logic for
     * handling such cases.
     * <p>
     * If new rates contain rates for periods that are not covered by the existing rates, the new rates will be added to
     * the existing rates.
     * <p>
     * Data is in status {@link com.rprelevic.xm.recom.api.model.DataStatus#GREEN} if all days for required
     * {@link com.rprelevic.xm.recom.api.model.SymbolProperties.TimeWindow} are present in the database.
     * Otherwise, data is in status {@link com.rprelevic.xm.recom.api.model.DataStatus#AMBER}.
     *
     * @param existingRates the existing rates
     * @param newRates      the new rates
     * @param startPeriod   the start period
     * @param endPeriod     the end period
     * @return the consolidation result
     */
    ConsolidationResult consolidate(List<Rate> existingRates, List<Rate> newRates, Instant startPeriod, Instant endPeriod);

}
