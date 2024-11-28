package com.rprelevic.xm.recom.adtout.csv;

import com.rprelevic.xm.recom.api.model.IngestionRequest;
import com.rprelevic.xm.recom.impl.SourceType;

public class CsvIngestionRequest extends IngestionRequest {

    public CsvIngestionRequest(String source, String symbol, SourceType sourceType) {
        super(source, symbol, sourceType);
    }
}
