package com.rprelevic.xm.recom.api.repository;

import com.rprelevic.xm.recom.api.model.Rate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for managing Rate entities.
 */
public interface RatesRepository {

    /**
     * Finds rates by symbol within a specified time window.
     *
     * @param symbol the symbol to search for
     * @param start the start of the time window
     * @param end the end of the time window
     * @return a list of rates matching the criteria
     */
    List<Rate> findRatesBySymbolAndInTimeWindow(String symbol, Instant start, Instant end);

    /**
     * Saves a list of consolidated rates.
     *
     * @param consolidatedRates the list of rates to save
     */
    void saveAll(List<Rate> consolidatedRates);

    /**
     * Finds all prices for a specific date.
     *
     * @param date the date to search for
     * @return a list of rates for the specified date
     */
    List<Rate> findAllPricesForDate(LocalDate date);

}