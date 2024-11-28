package com.rprelevic.xm.recom.api.repository;

import com.rprelevic.xm.recom.api.model.CryptoStats;

import java.time.LocalDateTime;
import java.util.Optional;

// TODO: Document all methods
public interface CryptoStatsRepository {

    void saveCryptoStats(CryptoStats cryptoStats);
    Optional<CryptoStats> findCryptoStatsBySymbolAndPeriodStart(String symbol, LocalDateTime periodStart);

}
