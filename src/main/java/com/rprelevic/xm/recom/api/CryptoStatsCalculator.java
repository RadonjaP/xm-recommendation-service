package com.rprelevic.xm.recom.api;

import com.rprelevic.xm.recom.api.model.ConsolidationResult;
import com.rprelevic.xm.recom.api.model.CryptoStats;

// TODO: Document interface
public interface CryptoStatsCalculator {

    CryptoStats calculateStats(ConsolidationResult result);

}
