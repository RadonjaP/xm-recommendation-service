package com.rprelevic.xm.recom.api.model;

import java.time.Instant;

public record Rate(Instant dateTime, String symbol, double rate) {
}
