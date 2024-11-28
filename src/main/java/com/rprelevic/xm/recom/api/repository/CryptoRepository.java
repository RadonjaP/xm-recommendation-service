package com.rprelevic.xm.recom.api.repository;

import com.rprelevic.xm.recom.api.model.Price;

import java.time.Instant;
import java.util.List;

// TODO: Document all methods
public interface CryptoRepository {

    List<Price> findPricesBySymbolAndInTimeWindow(String symbol, Instant start, Instant end);
    void saveAll(List<Price> consolidatedPrices);

}
