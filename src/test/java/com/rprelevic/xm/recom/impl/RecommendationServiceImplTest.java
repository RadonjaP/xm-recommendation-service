package com.rprelevic.xm.recom.impl;

import com.rprelevic.xm.recom.api.model.CryptoStats;
import com.rprelevic.xm.recom.api.model.Rate;
import com.rprelevic.xm.recom.api.model.SymbolProperties;
import com.rprelevic.xm.recom.api.repository.CryptoStatsRepository;
import com.rprelevic.xm.recom.api.repository.RatesRepository;
import com.rprelevic.xm.recom.api.repository.SymbolPropertiesRepository;
import com.rprelevic.xm.recom.impl.ex.SymbolNotSupportedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.rprelevic.xm.recom.api.model.SymbolProperties.TimeWindow.MONTHLY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class RecommendationServiceImplTest {

    @Mock
    private RatesRepository ratesRepository;
    @Mock
    private CryptoStatsRepository cryptoStatsRepository;
    @Mock
    private SymbolPropertiesRepository symbolPropertiesRepository;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void givenCryptoStats_whenListNormalized_thenSortedByNormalizedRange() {
        CryptoStats stat1 = CryptoStats.of("BTC", null, null, null, 0, 0, 0, 0, 0.5);
        CryptoStats stat2 = CryptoStats.of("ETH", null, null, null, 0, 0, 0, 0, 0.8);
        CryptoStats stat3 = CryptoStats.of("XRP", null, null, null, 0, 0, 0, 0, 0.3);
        when(cryptoStatsRepository.findLatestStatsForAllSymbols()).thenReturn(List.of(stat1, stat2, stat3));

        List<CryptoStats> result = recommendationService.listNormalized();

        assertEquals(List.of(stat2, stat1, stat3), result);
    }

    @Test
    void givenValidSymbol_whenGetCryptoStatsForSymbol_thenReturnCryptoStats() {
        String symbol = "BTC";
        SymbolProperties symbolProperties = new SymbolProperties(symbol, MONTHLY, false, LocalDateTime.now());
        CryptoStats cryptoStats = CryptoStats.of(symbol, null, null, null, 0, 0, 0, 0, 0.5);
        when(symbolPropertiesRepository.findSymbolProperties(symbol)).thenReturn(Optional.of(symbolProperties));
        when(cryptoStatsRepository.findLatestCryptoStatsBySymbol(symbol)).thenReturn(Optional.of(cryptoStats));

        CryptoStats result = recommendationService.getCryptoStatsForSymbol(symbol);

        assertEquals(cryptoStats, result);
    }

    @Test
    void givenInvalidSymbol_whenGetCryptoStatsForSymbol_thenThrowSymbolNotSupportedException() {
        String symbol = "INVALID";
        when(symbolPropertiesRepository.findSymbolProperties(symbol)).thenReturn(Optional.empty());

        assertThrows(SymbolNotSupportedException.class, () -> recommendationService.getCryptoStatsForSymbol(symbol));
    }

    @Test
    void givenDayWithRates_whenHighestNormalizedRangeForDay_thenReturnSymbolWithHighestNormalizedRange() {
        LocalDate day = LocalDate.of(2021, 10, 1);
        Rate rate1 = new Rate(Instant.now(), "BTC", 50000);
        Rate rate2 = new Rate(Instant.now(), "BTC", 60000);
        Rate rate3 = new Rate(Instant.now(), "ETH", 3000);
        Rate rate4 = new Rate(Instant.now(), "ETH", 4000);
        when(ratesRepository.findAllPricesForDate(day)).thenReturn(List.of(rate1, rate2, rate3, rate4));

        String result = recommendationService.highestNormalizedRangeForDay(day);

        assertEquals("ETH", result);
    }

    @Test
    void givenDayWithoutRates_whenHighestNormalizedRangeForDay_thenThrowIllegalArgumentException() {
        LocalDate day = LocalDate.of(2021, 10, 1);
        when(ratesRepository.findAllPricesForDate(day)).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> recommendationService.highestNormalizedRangeForDay(day));
    }

    @Test
    void givenEmptyCryptoStatsList_whenListNormalized_thenReturnEmptyList() {
        when(cryptoStatsRepository.findLatestStatsForAllSymbols()).thenReturn(List.of());

        List<CryptoStats> result = recommendationService.listNormalized();

        assertTrue(result.isEmpty());
    }

    @Test
    void givenNullCryptoStatsList_whenListNormalized_thenThrowNullPointerException() {
        when(cryptoStatsRepository.findLatestStatsForAllSymbols()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> recommendationService.listNormalized());
    }

    @Test
    void givenValidSymbol_whenGetCryptoStatsForSymbol_thenReturnCorrectStats() {
        String symbol = "ETH";
        SymbolProperties symbolProperties = new SymbolProperties(symbol, MONTHLY, false, LocalDateTime.now());
        CryptoStats cryptoStats = CryptoStats.of(symbol, null, null, null, 0, 0, 0, 0, 0.7);
        when(symbolPropertiesRepository.findSymbolProperties(symbol)).thenReturn(Optional.of(symbolProperties));
        when(cryptoStatsRepository.findLatestCryptoStatsBySymbol(symbol)).thenReturn(Optional.of(cryptoStats));

        CryptoStats result = recommendationService.getCryptoStatsForSymbol(symbol);

        assertEquals(cryptoStats, result);
    }

    @Test
    void givenDayWithSingleRate_whenHighestNormalizedRangeForDay_thenReturnSingleSymbol() {
        LocalDate day = LocalDate.of(2021, 10, 1);
        Rate rate = new Rate(Instant.now(), "BTC", 50000);
        when(ratesRepository.findAllPricesForDate(day)).thenReturn(List.of(rate));

        String result = recommendationService.highestNormalizedRangeForDay(day);

        assertEquals("BTC", result);
    }

    @Test
    void givenDayWithNullRates_whenHighestNormalizedRangeForDay_thenThrowNullPointerException() {
        LocalDate day = LocalDate.of(2021, 10, 1);
        when(ratesRepository.findAllPricesForDate(day)).thenReturn(null);

        assertThrows(NullPointerException.class, () -> recommendationService.highestNormalizedRangeForDay(day));
    }

    @Test
    void givenValidSymbol_whenGetCryptoStatsForSymbol_thenThrowIllegalArgumentException() {
        String symbol = "BTC";
        SymbolProperties symbolProperties = new SymbolProperties(symbol, MONTHLY, false, LocalDateTime.now());
        when(symbolPropertiesRepository.findSymbolProperties(symbol)).thenReturn(Optional.of(symbolProperties));
        when(cryptoStatsRepository.findLatestCryptoStatsBySymbol(symbol)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> recommendationService.getCryptoStatsForSymbol(symbol));
    }

}