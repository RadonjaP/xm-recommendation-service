package com.rprelevic.xm.recom.api;

import com.rprelevic.xm.recom.api.model.ConsolidationResult;
import com.rprelevic.xm.recom.api.model.Rate;

import java.time.Instant;
import java.util.List;

public interface RatesConsolidator {

    ConsolidationResult consolidate(List<Rate> existingRates, List<Rate> newRates, Instant startPeriod, Instant endPeriod);

}
