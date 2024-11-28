package com.rprelevic.xm.recom.api.model;

import java.time.Instant;

public record Price(Instant dateTime, String symbol, double rate) {
}
