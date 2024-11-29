package com.rprelevic.xm.recom.api.model;

import java.time.Instant;
import java.util.List;

/**
 * Represents the result of a consolidation operation.
 */
public record ConsolidationResult(List<Rate> consolidatedRates, DataStatus dataStatus, Instant periodStart,
                                  Instant periodEnd, String symbol) {
}
