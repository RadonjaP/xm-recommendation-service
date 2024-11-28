package com.rprelevic.xm.recom.api.model;

import java.time.Instant;
import java.util.List;

public record ConsolidationResult(List<Price> consolidatedPrices, DataStatus dataStatus, Instant periodStart,
                                  Instant periodEnd, String symbol) {
}
