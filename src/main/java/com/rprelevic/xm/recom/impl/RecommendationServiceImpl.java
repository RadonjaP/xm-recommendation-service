package com.rprelevic.xm.recom.impl;

import com.rprelevic.xm.recom.api.RecommendationService;
import com.rprelevic.xm.recom.api.model.CryptoStats;
import com.rprelevic.xm.recom.api.repository.CryptoRepository;
import com.rprelevic.xm.recom.api.repository.CryptoStatsRepository;
import com.rprelevic.xm.recom.api.repository.SymbolPropertiesRepository;

import java.time.LocalDate;
import java.util.List;

public class RecommendationServiceImpl implements RecommendationService {

    private final CryptoRepository cryptoRepository;
    private final CryptoStatsRepository cryptoStatsRepository;
    private final SymbolPropertiesRepository symbolPropertiesRepository;

    public RecommendationServiceImpl(CryptoRepository cryptoRepository, CryptoStatsRepository cryptoStatsRepository, SymbolPropertiesRepository symbolPropertiesRepository) {
        this.cryptoRepository = cryptoRepository;
        this.cryptoStatsRepository = cryptoStatsRepository;
        this.symbolPropertiesRepository = symbolPropertiesRepository;
    }

    @Override
    public List<String> listNormalized() {
        return List.of();
    }

    @Override
    public CryptoStats getCryptoStatsForSymbol(String symbol) {
        return null;
    }

    @Override
    public String highestNormalizedRangeForDay(LocalDate day) {
        return "";
    }
}
