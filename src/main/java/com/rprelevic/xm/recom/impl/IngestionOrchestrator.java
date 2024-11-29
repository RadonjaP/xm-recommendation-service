package com.rprelevic.xm.recom.impl;

import com.rprelevic.xm.recom.api.CryptoStatsCalculator;
import com.rprelevic.xm.recom.api.DataSourceReader;
import com.rprelevic.xm.recom.api.RatesConsolidator;
import com.rprelevic.xm.recom.api.model.*;
import com.rprelevic.xm.recom.api.repository.RatesRepository;
import com.rprelevic.xm.recom.api.repository.CryptoStatsRepository;
import com.rprelevic.xm.recom.api.repository.IngestionDetailsRepository;
import com.rprelevic.xm.recom.api.repository.SymbolPropertiesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.rprelevic.xm.recom.api.model.IngestionDetails.IngestionStatus.IN_PROGRESS;

public class IngestionOrchestrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngestionOrchestrator.class);

    private final DataSourceReader sourceReader;
    private final RatesRepository ratesRepository;
    private final IngestionDetailsRepository ingestionDetailsRepository;
    private final RatesConsolidator ratesConsolidator;
    private final SymbolPropertiesRepository symbolPropertiesRepository;
    private final CryptoStatsRepository cryptoStatsRepository;
    private final CryptoStatsCalculator cryptoStatsCalculator;

    public IngestionOrchestrator(DataSourceReader datasourceReader,
                                 RatesRepository ratesRepository,
                                 IngestionDetailsRepository ingestionDetailsRepository,
                                 RatesConsolidator ratesConsolidator,
                                 SymbolPropertiesRepository symbolPropertiesRepository,
                                 CryptoStatsRepository cryptoStatsRepository,
                                 CryptoStatsCalculator cryptoStatsCalculator) {
        this.sourceReader = datasourceReader;
        this.ratesRepository = ratesRepository;
        this.ingestionDetailsRepository = ingestionDetailsRepository;
        this.ratesConsolidator = ratesConsolidator;
        this.symbolPropertiesRepository = symbolPropertiesRepository;
        this.cryptoStatsRepository = cryptoStatsRepository;
        this.cryptoStatsCalculator = cryptoStatsCalculator;
    }

    public void ingest(IngestionRequest request) {
        // Store basic ingestion details in the database
        final String ingestionId = UUID.randomUUID().toString();
        final var ingestionDetails = IngestionDetails.builder()
                .ingestionId(ingestionId)
                .sourceName(request.source())
                .symbol(request.symbol())
                .sourceType(request.sourceType())
                .ingestionStartTime(LocalDateTime.now())
                .status(IN_PROGRESS)
                .build();
        try {

            ingestionDetailsRepository.save(ingestionDetails);

            // TODO: Consider adding ingestionId to property lock
            // Validate we can ingest the symbol and lock it
            verifyAndLockSymbol(request.symbol());

            // Read rates from datasource
            final List<Rate> newRates = sourceReader.readRates(request);

            // fetch the latest rates from the database for desired time window and consolidate them with new data
            final var startPeriod = newRates.get(newRates.size() - 1).dateTime();
            final var endPeriod = newRates.get(0).dateTime();

            final List<Rate> existingRates = ratesRepository
                    .findRatesBySymbolAndInTimeWindow(request.symbol(), startPeriod, endPeriod);

            final var consolidationResult = ratesConsolidator.consolidate(existingRates, newRates, startPeriod, endPeriod);

            // calculate statistics for desired time window
            final var timeWindowStats = cryptoStatsCalculator.calculateStats(consolidationResult);

            if (DataStatus.RED.equals(timeWindowStats.status())) {

                // Keep user notified about the RED status but do not store rate data in the database
                cryptoStatsRepository.saveCryptoStats(timeWindowStats);
                throw new RuntimeException("Data status is RED."); // TODO: Define exception
            }

            // Store rates in the database
            ratesRepository.saveAll(consolidationResult.consolidatedRates());

            // Store/update statistics in the database
            cryptoStatsRepository.saveCryptoStats(timeWindowStats);

            // Update ingestion information in the database
            ingestionDetailsRepository.ingestionSuccessful(ingestionDetails, consolidationResult.consolidatedRates().size());

        } catch (Exception e) {
            LOGGER.error("Symbol {} ingestion failed from source {}.", request.symbol(), request.source(), e);
            ingestionDetailsRepository.ingestionFailed(ingestionDetails);
        } finally {
            // Release the lock on the symbol
            symbolPropertiesRepository.unlockSymbol(request.symbol());
        }
    }

    private void verifyAndLockSymbol(String symbol) {
        final Optional<SymbolProperties> symbolProperties = symbolPropertiesRepository.findSymbolProperties(symbol);
        if (symbolProperties.isEmpty()) {
            throw new RuntimeException("Symbol %s is not supported.".formatted(symbol)); // TODO: Define exception
        }
        if (symbolProperties.get().locked()) {
            throw new RuntimeException("Symbol %s is already locked.".formatted(symbol)); // TODO: Define exception
        }

        symbolPropertiesRepository.lockSymbol(symbol)
                .orElseThrow(() -> new RuntimeException("Failed to lock the symbol %s".formatted(symbol))); // TODO: Define exception
    }

}
