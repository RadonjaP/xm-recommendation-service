package com.rprelevic.xm.recom.api.repository;

import com.rprelevic.xm.recom.api.model.SymbolProperties;

import java.util.Optional;

public interface SymbolPropertiesRepository {

    // Fetch symbol and validate if it's supported, record contains desired time window configuration (e.g. 1 month, 6 months, 1 year)
    Optional<SymbolProperties> findSymbolProperties(String symbol);
    Optional<Boolean> lockSymbol(String symbol);
    Optional<Boolean> unlockSymbol(String symbol);

}
