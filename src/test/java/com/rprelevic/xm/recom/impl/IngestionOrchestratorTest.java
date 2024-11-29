package com.rprelevic.xm.recom.impl;

import com.rprelevic.xm.recom.api.CryptoStatsCalculator;
import com.rprelevic.xm.recom.api.DataSourceReader;
import com.rprelevic.xm.recom.api.RatesConsolidator;
import com.rprelevic.xm.recom.api.model.*;
import com.rprelevic.xm.recom.api.model.IngestionDetails.IngestionStatus;
import com.rprelevic.xm.recom.api.repository.CryptoStatsRepository;
import com.rprelevic.xm.recom.api.repository.IngestionDetailsRepository;
import com.rprelevic.xm.recom.api.repository.RatesRepository;
import com.rprelevic.xm.recom.api.repository.SymbolPropertiesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static com.rprelevic.xm.recom.api.model.SymbolProperties.TimeWindow.MONTHLY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IngestionOrchestratorTest {

    @Mock
    private DataSourceReader sourceReader;
    @Mock
    private RatesRepository ratesRepository;
    @Mock
    private IngestionDetailsRepository ingestionDetailsRepository;
    @Mock
    private RatesConsolidator ratesConsolidator;
    @Mock
    private SymbolPropertiesRepository symbolPropertiesRepository;
    @Mock
    private CryptoStatsRepository cryptoStatsRepository;
    @Mock
    private CryptoStatsCalculator cryptoStatsCalculator;

    @InjectMocks
    private IngestionOrchestrator ingestionOrchestrator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void givenSuccessfulIngestion_whenIngest_thenSaveRatesAndCryptoInfo() throws Exception {
        IngestionRequest request = new IngestionRequest("source", "BTC", SourceType.API);
        SymbolProperties symbolProperties = new SymbolProperties("BTC", MONTHLY, false, null);
        List<Rate> rates = List.of(new Rate(Instant.now(), "BTC", 100.0));
        List<Rate> existingRates = List.of(new Rate(Instant.now().minus(1, ChronoUnit.DAYS), "BTC", 90.0));
        ConsolidationResult consolidationResult =
                new ConsolidationResult(rates, DataStatus.GREEN, Instant.now().minus(1, ChronoUnit.DAYS), Instant.now(), "BTC");

        when(symbolPropertiesRepository.findSymbolProperties("BTC")).thenReturn(Optional.of(symbolProperties));
        when(symbolPropertiesRepository.lockSymbol("BTC")).thenReturn(Optional.of(true));
        when(sourceReader.readRates(request)).thenReturn(rates);
        when(ratesRepository.findRatesBySymbolAndInTimeWindow(eq("BTC"), any(Instant.class), any(Instant.class))).thenReturn(existingRates);
        when(ratesConsolidator.consolidate(eq(existingRates), eq(rates), any(Instant.class), any(Instant.class))).thenReturn(consolidationResult);
        when(cryptoStatsCalculator.calculateStats(consolidationResult))
                .thenReturn(new CryptoStats(LocalDateTime.now(), LocalDateTime.now(), "BTC", DataStatus.GREEN,
                        100.0, 100.0, 100.0, 100.0, 0.5));

        ingestionOrchestrator.ingest(request);

        verify(ratesRepository).saveAll(rates);
        verify(cryptoStatsRepository).saveCryptoStats(any(CryptoStats.class));
        verify(ingestionDetailsRepository).save(any(IngestionDetails.class));
        verify(ingestionDetailsRepository)
                .ingestionSuccessful(argThat(
                        details -> details.status().equals(IngestionStatus.IN_PROGRESS)), eq(1));
        verify(symbolPropertiesRepository).unlockSymbol("BTC");
    }

    @Test
    void givenFailedToLockSymbol_whenIngest_thenUpdateStatusToFailure() {
        IngestionRequest request = new IngestionRequest("source", "BTC", SourceType.API);
        SymbolProperties symbolProperties = new SymbolProperties("BTC", MONTHLY, true, null);

        when(symbolPropertiesRepository.findSymbolProperties("BTC")).thenReturn(Optional.of(symbolProperties));

        ingestionOrchestrator.ingest(request);

        verify(ingestionDetailsRepository)
                .ingestionFailed(argThat(details -> details.status().equals(IngestionStatus.IN_PROGRESS)));
        verify(symbolPropertiesRepository, never()).lockSymbol("BTC");
    }

    @Test
    void givenDataStatusRed_whenIngest_thenUpdateStatusToFailure() throws Exception {
        IngestionRequest request = new IngestionRequest("source", "BTC", SourceType.API);
        SymbolProperties symbolProperties = new SymbolProperties("BTC", MONTHLY, false, null);
        List<Rate> rates = List.of(new Rate(Instant.now(), "BTC", 100.0));
        List<Rate> existingRates = List.of(new Rate(Instant.now().minus(1, ChronoUnit.DAYS), "BTC", 90.0));
        ConsolidationResult consolidationResult = new ConsolidationResult(rates, DataStatus.RED,
                Instant.now().minus(1, ChronoUnit.DAYS), Instant.now(), "BTC");
        
        when(symbolPropertiesRepository.findSymbolProperties("BTC")).thenReturn(Optional.of(symbolProperties));
        when(symbolPropertiesRepository.lockSymbol("BTC")).thenReturn(Optional.of(true));
        when(sourceReader.readRates(request)).thenReturn(rates);
        when(ratesRepository.findRatesBySymbolAndInTimeWindow(eq("BTC"), any(Instant.class), any(Instant.class))).thenReturn(existingRates);
        when(ratesConsolidator.consolidate(eq(existingRates), eq(rates), any(Instant.class), any(Instant.class))).thenReturn(consolidationResult);
        when(cryptoStatsCalculator.calculateStats(consolidationResult))
                .thenReturn(new CryptoStats(LocalDateTime.now(), LocalDateTime.now(), "BTC", DataStatus.RED,
                        100.0, 100.0, 100.0, 100.0, 0.5));

        ingestionOrchestrator.ingest(request);

        verify(ratesRepository, never()).saveAll(rates);
        verify(cryptoStatsRepository).saveCryptoStats(any(CryptoStats.class));
        verify(ingestionDetailsRepository)
                .ingestionFailed(argThat(details -> details.status().equals(IngestionStatus.IN_PROGRESS)));
        verify(symbolPropertiesRepository).unlockSymbol("BTC");
    }

    @Test
    void givenExceptionDuringIngestion_whenIngest_thenUpdateStatusToFailure() throws Exception {
        IngestionRequest request = new IngestionRequest("source", "BTC", SourceType.API);
        SymbolProperties symbolProperties = new SymbolProperties("BTC", MONTHLY, false, null);

        when(symbolPropertiesRepository.findSymbolProperties("BTC")).thenReturn(Optional.of(symbolProperties));
        when(symbolPropertiesRepository.lockSymbol("BTC")).thenReturn(Optional.of(true));
        when(sourceReader.readRates(request)).thenThrow(new RuntimeException("Test Exception"));

        ingestionOrchestrator.ingest(request);

        verify(ingestionDetailsRepository)
                .ingestionFailed(argThat(details -> details.status().equals(IngestionStatus.IN_PROGRESS)));
        verify(symbolPropertiesRepository).unlockSymbol("BTC");
    }
}