package com.rprelevic.xm.recom;

import com.rprelevic.xm.recom.adtout.h2.RatesRepositoryH2;
import com.rprelevic.xm.recom.api.model.DataStatus;
import com.rprelevic.xm.recom.api.model.IngestionRequest;
import com.rprelevic.xm.recom.api.model.Rate;
import com.rprelevic.xm.recom.api.repository.CryptoStatsRepository;
import com.rprelevic.xm.recom.api.repository.SymbolPropertiesRepository;
import com.rprelevic.xm.recom.cfg.ApplicationConfig;
import com.rprelevic.xm.recom.cfg.TestSecurityConfig;
import com.rprelevic.xm.recom.impl.IngestionOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration
@ActiveProfiles("test")
@ComponentScan(basePackages = {"com.rprelevic.xm.recom.adtin.rest", "com.rprelevic.xm.recom.adtout.h2"})
@ContextConfiguration(classes = {TestSecurityConfig.class, ApplicationConfig.class})
class IngestionControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoSpyBean
    private IngestionOrchestrator ingestionOrchestrator;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RatesRepositoryH2 ratesRepository;

    @Autowired
    private CryptoStatsRepository cryptoStatsRepository;

    @Autowired
    private SymbolPropertiesRepository symbolPropertiesRepository;

    @BeforeEach
    void setUp() throws Exception {
    }

    @Test
    void givenSourceFileExists_whenDataIsIngested_thenDatabaseStateChanges() throws Exception {
        mockMvc.perform(get("/api/v1/ingestion/start"))
                .andExpect(status().isOk());

        LocalDateTime ingestionStart = LocalDateTime.now();

        // Wait for ingestion to finish (async processing)
        Thread.sleep(1000);

        verify(ingestionOrchestrator, times(5)).ingest(any(IngestionRequest.class));

        // Prices for BTC are in database at count of 100, ingestion successful, data is GREEN
        String btcSQL = "SELECT COUNT(*) FROM ingestion_details WHERE status = 'SUCCESS' AND symbol = 'BTC'";
        int countBtc = jdbcTemplate.queryForObject(btcSQL, Integer.class);
        assertEquals(1, countBtc);
        // Prices for BTC are in database at count of 100
        assertRatesAreIngested("BTC", 100);
        // CryptoStats for BTC are in database calculated properly with status GREEN
        assertCryptoStats("BTC", DataStatus.GREEN, 33276.59, 47722.66,
                38415.79, 46813.21, 0.43412110435594536);
        assertLockUpdatedAndReleased("BTC", ingestionStart);

        // Prices for LTC are in database at count of 85, ingestion successful, data is AMBER
        String ltcSQL = "SELECT COUNT(*) FROM ingestion_details WHERE status = 'SUCCESS' AND symbol = 'LTC'";
        int countLtc = jdbcTemplate.queryForObject(ltcSQL, Integer.class);
        assertEquals(1, countLtc);
        // Prices for LTC are in database at count of 85
        assertRatesAreIngested("LTC", 85);
        // CryptoStats for LTC are in database calculated properly with status AMBER
        assertCryptoStats("LTC", DataStatus.AMBER, 103.4, 151.5, 109.6, 148.1, 0.4651837524177949);
        assertLockUpdatedAndReleased("LTC", ingestionStart);


        // Prices for NNX are not in database, NNX not supported, ingestion failed
        String sqlNNX = "SELECT COUNT(*) FROM ingestion_details WHERE status = 'FAILURE' AND symbol = 'NNX'";
        int countNnx = jdbcTemplate.queryForObject(sqlNNX, Integer.class);
        assertEquals(1, countNnx);
        // Prices for NNX are not in database
        assertRatesAreIngested("NNX", 0);
    }

    private void assertRatesAreIngested(String symbol, int size) {
        List<Rate> rates = ratesRepository.findRatesBySymbolAndInTimeWindow(symbol,
                Instant.ofEpochMilli(1640905200000L), // 2021-12-31T00:00:00Z
                Instant.now());
        assertEquals(size, rates.size());
    }

    private void assertLockUpdatedAndReleased(String symbol, LocalDateTime ingestionStart) {
        symbolPropertiesRepository.findSymbolProperties(symbol)
                .ifPresentOrElse(symbolProperties -> {
                    LocalDateTime lockedAt = symbolProperties.lockedAt();
                    assertFalse(symbolProperties.locked());
                    assertEquals(lockedAt.truncatedTo(ChronoUnit.SECONDS), ingestionStart.truncatedTo(ChronoUnit.SECONDS));
                }, () -> {
                    throw new RuntimeException("SymbolProperties for BTC not found");
                });
    }

    private void assertCryptoStats(String symbol, DataStatus status, double minRate, double maxRate,
                                   double latestRate, double oldestRate, double normalizedRange) {
        cryptoStatsRepository.findLatestCryptoStatsBySymbol(symbol)
                .ifPresentOrElse(cryptoStats -> {
                    assertEquals(symbol, cryptoStats.symbol());
                    assertEquals(status, cryptoStats.status());
                    assertEquals(minRate, cryptoStats.minRate());
                    assertEquals(maxRate, cryptoStats.maxRate());
                    assertEquals(latestRate, cryptoStats.latestRate());
                    assertEquals(oldestRate, cryptoStats.oldestRate());
                    assertEquals(normalizedRange, cryptoStats.normalizedRange());
                }, () -> {
                    throw new RuntimeException("CryptoStats for " + symbol + " not found");
                });
    }
}