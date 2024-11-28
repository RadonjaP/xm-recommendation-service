package com.rprelevic.xm.recom.impl;

import com.rprelevic.xm.recom.api.DataSourceReader;
import com.rprelevic.xm.recom.api.PriceConsolidator;
import com.rprelevic.xm.recom.api.model.*;
import com.rprelevic.xm.recom.api.model.IngestionDetails.IngestionStatus;
import com.rprelevic.xm.recom.api.repository.CryptoRepository;
import com.rprelevic.xm.recom.api.repository.CryptoStatsRepository;
import com.rprelevic.xm.recom.api.repository.IngestionDetailsRepository;
import com.rprelevic.xm.recom.api.repository.SymbolPropertiesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
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
    private CryptoRepository cryptoRepository;
    @Mock
    private IngestionDetailsRepository ingestionDetailsRepository;
    @Mock
    private PriceConsolidator priceConsolidator;
    @Mock
    private SymbolPropertiesRepository symbolPropertiesRepository;
    @Mock
    private CryptoStatsRepository cryptoStatsRepository;

    @InjectMocks
    private IngestionOrchestrator ingestionOrchestrator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void givenSuccessfulIngestion_whenIngest_thenSavePricesAndCryptoInfo() throws Exception {
        IngestionRequest request = new IngestionRequest("source", "BTC", SourceType.API);
        SymbolProperties symbolProperties = new SymbolProperties("BTC", MONTHLY, false, null);
        List<Price> prices = List.of(new Price(Instant.now(), "BTC", 100.0));
        List<Price> existingPrices = List.of(new Price(Instant.now().minus(1, ChronoUnit.DAYS), "BTC", 90.0));
        ConsolidationResult consolidationResult =
                new ConsolidationResult(prices, DataStatus.GREEN, Instant.now().minus(1, ChronoUnit.DAYS), Instant.now(), "BTC");

        when(symbolPropertiesRepository.findSymbolProperties("BTC")).thenReturn(Optional.of(symbolProperties));
        when(symbolPropertiesRepository.lockSymbol("BTC")).thenReturn(Optional.of(true));
        when(sourceReader.readPrices(request)).thenReturn(prices);
        when(cryptoRepository.findPricesBySymbolAndInTimeWindow(eq("BTC"), any(Instant.class), any(Instant.class))).thenReturn(existingPrices);
        when(priceConsolidator.consolidate(eq(existingPrices), eq(prices), any(Instant.class), any(Instant.class))).thenReturn(consolidationResult);

        ingestionOrchestrator.ingest(request);

        verify(cryptoRepository).saveAll(prices);
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
        List<Price> prices = List.of(new Price(Instant.now(), "BTC", 100.0));
        List<Price> existingPrices = List.of(new Price(Instant.now().minus(1, ChronoUnit.DAYS), "BTC", 90.0));
        ConsolidationResult consolidationResult = new ConsolidationResult(prices, DataStatus.RED, Instant.now().minus(1, ChronoUnit.DAYS), Instant.now(), "BTC");
        ;

        when(symbolPropertiesRepository.findSymbolProperties("BTC")).thenReturn(Optional.of(symbolProperties));
        when(symbolPropertiesRepository.lockSymbol("BTC")).thenReturn(Optional.of(true));
        when(sourceReader.readPrices(request)).thenReturn(prices);
        when(cryptoRepository.findPricesBySymbolAndInTimeWindow(eq("BTC"), any(Instant.class), any(Instant.class))).thenReturn(existingPrices);
        when(priceConsolidator.consolidate(eq(existingPrices), eq(prices), any(Instant.class), any(Instant.class))).thenReturn(consolidationResult);

        ingestionOrchestrator.ingest(request);

        verify(cryptoRepository, never()).saveAll(prices);
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
        when(sourceReader.readPrices(request)).thenThrow(new RuntimeException("Test Exception"));

        ingestionOrchestrator.ingest(request);

        verify(ingestionDetailsRepository)
                .ingestionFailed(argThat(details -> details.status().equals(IngestionStatus.IN_PROGRESS)));
        verify(symbolPropertiesRepository).unlockSymbol("BTC");
    }
}