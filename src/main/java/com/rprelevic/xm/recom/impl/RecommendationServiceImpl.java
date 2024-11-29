package com.rprelevic.xm.recom.impl;

import com.rprelevic.xm.recom.api.RecommendationService;
import com.rprelevic.xm.recom.api.model.CryptoStats;
import com.rprelevic.xm.recom.api.model.Rate;
import com.rprelevic.xm.recom.api.repository.CryptoStatsRepository;
import com.rprelevic.xm.recom.api.repository.RatesRepository;
import com.rprelevic.xm.recom.api.repository.SymbolPropertiesRepository;
import com.rprelevic.xm.recom.impl.ex.SymbolNotSupportedException;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RecommendationServiceImpl implements RecommendationService {

    private final RatesRepository ratesRepository;
    private final CryptoStatsRepository cryptoStatsRepository;
    private final SymbolPropertiesRepository symbolPropertiesRepository;

    public RecommendationServiceImpl(RatesRepository ratesRepository, CryptoStatsRepository cryptoStatsRepository, SymbolPropertiesRepository symbolPropertiesRepository) {
        this.ratesRepository = ratesRepository;
        this.cryptoStatsRepository = cryptoStatsRepository;
        this.symbolPropertiesRepository = symbolPropertiesRepository;
    }

    @Override
    public List<CryptoStats> listNormalized() {
        return cryptoStatsRepository.findLatestStatsForAllSymbols().stream()
                .sorted((a, b) -> Double.compare(b.normalizedRange(), a.normalizedRange()))
                .collect(Collectors.toList());
    }

    @Override
    public CryptoStats getCryptoStatsForSymbol(String symbol) {
        // Check if the symbol is supported
        // TODO: Cache the symbol properties
        final var symbolProperties = symbolPropertiesRepository.findSymbolProperties(symbol)
                .orElseThrow(() -> new SymbolNotSupportedException("Symbol not supported: " + symbol));

        // Retrieve the latest crypto stats for the symbol
        return cryptoStatsRepository
                .findLatestCryptoStatsBySymbol(symbolProperties.symbol())
                .orElseThrow(() -> new IllegalArgumentException("No stats found for symbol: " + symbol)); // TODO: Exception handling
    }

    @Override
    public String highestNormalizedRangeForDay(LocalDate day) {
        // Fetch all rates for the given day
        final var rates = ratesRepository.findAllPricesForDate(day);
        if (rates.isEmpty()) {
            throw new IllegalArgumentException("No rates found for the given day: %s".formatted(day)); // TODO: Exception handling
        }

        // Group rates by symbol and calculate normalization rate for each symbol
        return rates.stream()
                .collect(Collectors.groupingBy(Rate::symbol))
                .entrySet()
                .stream()
                .map(this::mapToSymbolAndNormalizedRangePair)
                .max(Comparator.comparingDouble(Pair::getRight))
                .map(Pair::getLeft)
                .orElse("No symbol found");
    }

    private Pair<String, Double> mapToSymbolAndNormalizedRangePair(Map.Entry<String, List<Rate>> entry) {
        String symbol = entry.getKey();
        List<Rate> symbolRates = entry.getValue();
        double minRate = symbolRates.stream().mapToDouble(Rate::rate).min().orElse(0);
        double maxRate = symbolRates.stream().mapToDouble(Rate::rate).max().orElse(0);
        Double normalizedRange = (maxRate - minRate) / minRate;

        return Pair.of(symbol, normalizedRange);
    }
}
