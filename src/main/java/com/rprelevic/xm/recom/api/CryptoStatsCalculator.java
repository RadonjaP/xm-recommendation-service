package com.rprelevic.xm.recom.api;

import com.rprelevic.xm.recom.api.model.ConsolidationResult;
import com.rprelevic.xm.recom.api.model.CryptoStats;

/**
 * Interface for calculating cryptocurrency statistics.
 */
public interface CryptoStatsCalculator {

    /**
     * Calculates the statistics for a given consolidation result.
     *
     * @param result the consolidation result containing the data to calculate statistics from
     * @return the calculated cryptocurrency statistics
     */
    CryptoStats calculateStats(ConsolidationResult result);

}
