package com.rprelevic.xm.recom.api.repository;

import com.rprelevic.xm.recom.api.model.CryptoStats;

import java.util.List;
import java.util.Optional;

// TODO: Document all methods
public interface CryptoStatsRepository {

    void saveCryptoStats(CryptoStats cryptoStats);
    Optional<CryptoStats> findLatestCryptoStatsBySymbol(String symbol);
    List<CryptoStats> findLatestStatsForAllSymbols();    // TODO: Write test for this method
}
