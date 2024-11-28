package com.rprelevic.xm.recom.impl;

import com.rprelevic.xm.recom.api.CryptoStatsCalculator;
import com.rprelevic.xm.recom.api.DataSourceReader;
import com.rprelevic.xm.recom.api.PriceConsolidator;
import com.rprelevic.xm.recom.api.model.*;
import com.rprelevic.xm.recom.api.repository.CryptoRepository;
import com.rprelevic.xm.recom.api.repository.CryptoStatsRepository;
import com.rprelevic.xm.recom.api.repository.IngestionDetailsRepository;
import com.rprelevic.xm.recom.api.repository.SymbolPropertiesRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.rprelevic.xm.recom.api.model.IngestionDetails.IngestionStatus.IN_PROGRESS;

public class IngestionOrchestrator {

    // TODO: Add logging

    private final DataSourceReader sourceReader;
    private final CryptoRepository cryptoRepository;
    private final IngestionDetailsRepository ingestionDetailsRepository;
    private final PriceConsolidator priceConsolidator;
    private final SymbolPropertiesRepository symbolPropertiesRepository;
    private final CryptoStatsRepository cryptoStatsRepository;
    private final CryptoStatsCalculator cryptoStatsCalculator;

    public IngestionOrchestrator(DataSourceReader datasourceReader,
                                 CryptoRepository cryptoRepository,
                                 IngestionDetailsRepository ingestionDetailsRepository,
                                 PriceConsolidator priceConsolidator,
                                 SymbolPropertiesRepository symbolPropertiesRepository,
                                 CryptoStatsRepository cryptoStatsRepository,
                                 CryptoStatsCalculator cryptoStatsCalculator) {
        this.sourceReader = datasourceReader;
        this.cryptoRepository = cryptoRepository;
        this.ingestionDetailsRepository = ingestionDetailsRepository;
        this.priceConsolidator = priceConsolidator;
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

            // Read prices from datasource
            final List<Price> newPrices = sourceReader.readPrices(request);

            // fetch the latest prices from the database for desired time window and consolidate them with new data
            final var startPeriod = newPrices.get(0).dateTime();
            final var endPeriod = newPrices.get(newPrices.size() - 1).dateTime();
            final List<Price> existingPrices = cryptoRepository
                    .findPricesBySymbolAndInTimeWindow(request.symbol(), startPeriod, endPeriod);

            final var consolidationResult = priceConsolidator.consolidate(existingPrices, newPrices, startPeriod, endPeriod);

            // calculate statistics for desired time window
            final var timeWindowStats = cryptoStatsCalculator.calculateStats(consolidationResult);

            if (DataStatus.RED.equals(timeWindowStats.status())) {

                // Keep user notified about the RED status but do not store rate data in the database
                cryptoStatsRepository.saveCryptoStats(timeWindowStats);
                throw new RuntimeException("Data status is RED"); // TODO: Define exception
            }

            // store prices in the database (if no errors occurred, aka. ingestion is SUCCESS and stats are AMBER or GREEN)
            cryptoRepository.saveAll(consolidationResult.consolidatedPrices());

            // store/update statistics in the database
            cryptoStatsRepository.saveCryptoStats(timeWindowStats);

            // Update ingestion information in the database - ingestion status (SUCCESS, FAILURE),
            // number of records ingested, ingestion end time
            ingestionDetailsRepository.ingestionSuccessful(ingestionDetails, consolidationResult.consolidatedPrices().size());

        } catch (Exception e) {
            ingestionDetailsRepository.ingestionFailed(ingestionDetails);
            return;
        } finally {
            // Release the lock on the symbol
            symbolPropertiesRepository.unlockSymbol(request.symbol());
        }
    }

    private void verifyAndLockSymbol(String symbol) {
        final Optional<SymbolProperties> symbolProperties = symbolPropertiesRepository.findSymbolProperties(symbol);
        if (symbolProperties.isEmpty() || symbolProperties.get().locked()) {
            throw new RuntimeException("Symbol is not supported or is locked"); // TODO: Define exception
        }

        symbolPropertiesRepository.lockSymbol(symbol)
                .orElseThrow(() -> new RuntimeException("Failed to lock the symbol")); // TODO: Define exception
    }

}
