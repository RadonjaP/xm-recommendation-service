package com.rprelevic.xm.recom.api.repository;

import com.rprelevic.xm.recom.api.model.CryptoStats;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing CryptoStats entities.
 */
public interface CryptoStatsRepository {

    /**
     * Saves the given {@link CryptoStats} entity.
     *
     * @param cryptoStats the {@link CryptoStats} entity to save
     */
    void saveCryptoStats(CryptoStats cryptoStats);

    /**
     * Finds the latest {@link CryptoStats} entity by the given symbol.
     *
     * @param symbol the symbol to search for
     * @return an Optional containing the latest {@link CryptoStats} entity if found, otherwise empty
     */
    Optional<CryptoStats> findLatestCryptoStatsBySymbol(String symbol);

    /**
     * Finds the latest {@link CryptoStats} entities for all symbols.
     *
     * @return a list of the latest {@link CryptoStats} entities for all symbols
     */
    List<CryptoStats> findLatestStatsForAllSymbols();
}