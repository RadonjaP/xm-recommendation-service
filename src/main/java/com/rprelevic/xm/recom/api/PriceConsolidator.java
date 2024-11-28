package com.rprelevic.xm.recom.api;

import com.rprelevic.xm.recom.api.model.ConsolidationResult;
import com.rprelevic.xm.recom.api.model.Price;

import java.time.Instant;
import java.util.List;

public interface PriceConsolidator {

    // if data for time window is not COMPLETELY available in the database, status of stats will be AMBER, ingestion status will be SUCCESS
    // if data for time window is available in the database status of stats will be GREEN, ingestion status will be SUCCESS
    ConsolidationResult consolidate(List<Price> existingPrices, List<Price> newPrices, Instant startPeriod, Instant endPeriod);

}
