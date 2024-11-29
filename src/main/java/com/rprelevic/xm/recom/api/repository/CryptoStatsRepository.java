package com.rprelevic.xm.recom.api.repository;

import com.rprelevic.xm.recom.api.model.CryptoStats;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing CryptoStats entities.
 */
public interface CryptoStatsRepository {

    /**
     * Saves the given CryptoStats entity.
     *
     * @param cryptoStats the CryptoStats entity to save
     */
    void saveCryptoStats(CryptoStats cryptoStats);

    /**
     * Finds the latest CryptoStats entity by the given symbol.
     *
     * @param symbol the symbol to search for
     * @return an Optional containing the latest CryptoStats entity if found, otherwise empty
     */
    Optional<CryptoStats> findLatestCryptoStatsBySymbol(String symbol);

    /**
     * Finds the latest CryptoStats entities for all symbols.
     *
     * @return a list of the latest CryptoStats entities for all symbols
     */
    List<CryptoStats> findLatestStatsForAllSymbols();
}