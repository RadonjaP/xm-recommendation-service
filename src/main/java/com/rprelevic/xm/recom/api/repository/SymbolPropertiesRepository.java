package com.rprelevic.xm.recom.api.repository;

import com.rprelevic.xm.recom.api.model.SymbolProperties;

import java.util.Optional;

/**
 * Repository interface for managing {@link SymbolProperties} entities.
 */
public interface SymbolPropertiesRepository {

    /**
     * Finds the {@link SymbolProperties} for the given symbol.
     *
     * @param symbol the symbol to search for
     * @return an {@link Optional} containing the found {@link SymbolProperties}, or empty if not found
     */
    Optional<SymbolProperties> findSymbolProperties(String symbol);

    /**
     * Locks the given symbol.
     *
     * @param symbol the symbol to lock
     * @return an {@link Optional} containing {@code true} if the symbol was successfully locked, or {@code false} otherwise
     */
    Optional<Boolean> lockSymbol(String symbol);

    /**
     * Unlocks the given symbol.
     *
     * @param symbol the symbol to unlock
     * @return an {@link Optional} containing {@code true} if the symbol was successfully unlocked, or {@code false} otherwise
     */
    Optional<Boolean> unlockSymbol(String symbol);

}