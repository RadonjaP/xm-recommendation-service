package com.rprelevic.xm.recom.api.repository;

import com.rprelevic.xm.recom.api.model.Rate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

// TODO: Document all methods
public interface RatesRepository {

    List<Rate> findRatesBySymbolAndInTimeWindow(String symbol, Instant start, Instant end);

    void saveAll(List<Rate> consolidatedRates);

    List<Rate> findAllPricesForDate(LocalDate date);

}
