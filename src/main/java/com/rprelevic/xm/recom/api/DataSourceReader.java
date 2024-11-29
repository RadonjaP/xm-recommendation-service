package com.rprelevic.xm.recom.api;

import com.rprelevic.xm.recom.api.model.IngestionRequest;
import com.rprelevic.xm.recom.api.model.Rate;

import java.util.List;

/**
 * Exposes functionalities related to reading data from a datasource.
 * Following the Hexagonal Architecture pattern, this interface represents Outbound Adapter and
 * can have different implementations for various sources.
 *
 */
public interface DataSourceReader {

    /**
     * Reads the rates from the datasource based on the request.
     *
     * @param request - the request object containing relevant information for datasource extraction.
     * @return - a list of rates extracted from the datasource
     */
    List<Rate> readRates(IngestionRequest request);

}
