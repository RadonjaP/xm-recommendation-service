package com.rprelevic.xm.recom.adtout.csv;

import com.rprelevic.xm.recom.api.model.IngestionRequest;
import com.rprelevic.xm.recom.impl.SourceType;

/**
 * Ingestion request for CSV source type.
 */
public class CsvIngestionRequest extends IngestionRequest {

    /**
     * Constructor.
     *
     * @param source     the source (file path, url, etc.)
     * @param symbol     the symbol (e.g. BTC, ETH, etc.)
     * @param sourceType the source type
     */
    public CsvIngestionRequest(String source, String symbol, SourceType sourceType) {
        super(source, symbol, sourceType);
    }
}
