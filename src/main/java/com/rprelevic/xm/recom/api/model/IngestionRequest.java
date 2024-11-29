package com.rprelevic.xm.recom.api.model;

import com.rprelevic.xm.recom.impl.SourceType;

/**
 * Represents an ingestion request.
 */
public class IngestionRequest {
    private final String source;
    private final String symbol;
    private final SourceType sourceType;

    public IngestionRequest(String source, String symbol, SourceType sourceType) {
        this.source = source;
        this.symbol = symbol;
        this.sourceType = sourceType;
    }

    public String source() {
        return source;
    }

    public String symbol() {
        return symbol;
    }

    public SourceType sourceType() {
        return sourceType;
    }
}
