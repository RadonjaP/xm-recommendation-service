package com.rprelevic.xm.recom.api.model;

import java.time.Instant;

/**
 * Represents a rate given from data source.
 */
public record Rate(Instant dateTime, String symbol, double rate) {
}
