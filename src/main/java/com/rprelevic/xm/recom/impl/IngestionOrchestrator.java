package com.rprelevic.xm.recom.impl;

import com.rprelevic.xm.recom.api.CryptoStatsCalculator;
import com.rprelevic.xm.recom.api.DataSourceReader;
import com.rprelevic.xm.recom.api.RatesConsolidator;
import com.rprelevic.xm.recom.api.model.IngestionDetails;
import com.rprelevic.xm.recom.api.model.IngestionRequest;
import com.rprelevic.xm.recom.api.model.Rate;
import com.rprelevic.xm.recom.api.model.SymbolProperties;
import com.rprelevic.xm.recom.api.repository.CryptoStatsRepository;
import com.rprelevic.xm.recom.api.repository.IngestionDetailsRepository;
import com.rprelevic.xm.recom.api.repository.RatesRepository;
import com.rprelevic.xm.recom.api.repository.SymbolPropertiesRepository;
import com.rprelevic.xm.recom.impl.ex.FailedToObtainLockException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.rprelevic.xm.recom.api.model.DataStatus.RED;
import static com.rprelevic.xm.recom.api.model.IngestionDetails.IngestionStatus.IN_PROGRESS;

/**
 * Orchestrates the ingestion process for financial data.
 */
public class IngestionOrchestrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngestionOrchestrator.class);

    private final DataSourceReader sourceReader;
    private final RatesRepository ratesRepository;
    private final IngestionDetailsRepository ingestionDetailsRepository;
    private final RatesConsolidator ratesConsolidator;
    private final SymbolPropertiesRepository symbolPropertiesRepository;
    private final CryptoStatsRepository cryptoStatsRepository;
    private final CryptoStatsCalculator cryptoStatsCalculator;

    /**
     * Constructs an IngestionOrchestrator with the necessary dependencies.
     *
     * @param datasourceReader the data source reader
     * @param ratesRepository the rates repository
     * @param ingestionDetailsRepository the ingestion details repository
     * @param ratesConsolidator the rates consolidator
     * @param symbolPropertiesRepository the symbol properties repository
     * @param cryptoStatsRepository the crypto stats repository
     * @param cryptoStatsCalculator the crypto stats calculator
     */
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

    /**
     * Ingests data based on the given request.
     *
     * @param request the ingestion request
     */
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
            // Validate we can ingest the symbol and lock it
            final var symbolProperties = verifyAndLockSymbol(request.symbol());

            ingestionDetailsRepository.save(ingestionDetails);

            // Read rates from datasource
            final List<Rate> newRates = sourceReader.readRates(request);
            if (newRates.isEmpty()) {
                LOGGER.error("No input rates found for symbol {} in source {}.", request.symbol(), request.source());
                throw new RuntimeException("No input rates found for symbol %s in source %s.".formatted(request.symbol(), request.source()));
            }

            // Fetch the latest rates from the database for desired time window and consolidate them with new data
            final var startAndEndPeriodPair = findStartAndEndPeriod(newRates, symbolProperties);
            final List<Rate> existingRates = ratesRepository
                    .findRatesBySymbolAndInTimeWindow(request.symbol(),
                            startAndEndPeriodPair.getLeft(), startAndEndPeriodPair.getRight());

            final var consolidationResult = ratesConsolidator.consolidate(existingRates, newRates,
                    startAndEndPeriodPair.getLeft(), startAndEndPeriodPair.getRight());

            // calculate statistics for desired time window
            final var timeWindowStats = cryptoStatsCalculator.calculateStats(consolidationResult);

            if (RED.equals(timeWindowStats.status())) {

                // Keep user notified about the RED status but do not store rate data in the database
                cryptoStatsRepository.saveCryptoStats(timeWindowStats);
                throw new RuntimeException("Data status is RED.");
            }

            // Store rates in the database
            ratesRepository.saveAll(consolidationResult.consolidatedRates());

            // Store/update statistics in the database
            cryptoStatsRepository.saveCryptoStats(timeWindowStats);

            // Update ingestion information in the database
            ingestionDetailsRepository.ingestionSuccessful(ingestionDetails, consolidationResult.consolidatedRates().size());

        } catch (FailedToObtainLockException e) {
            LOGGER.error("Failed to obtain lock for symbol {}. Ingestion failed from source {}.", request.symbol(), request.source(), e);
            ingestionDetailsRepository.ingestionFailed(ingestionDetails);
            return;
        } catch (Exception e) {
            LOGGER.error("Symbol {} ingestion failed from source {}.", request.symbol(), request.source(), e);
            ingestionDetailsRepository.ingestionFailed(ingestionDetails);
        }

        // Release the lock on the symbol
        symbolPropertiesRepository.unlockSymbol(request.symbol());
    }

    /**
     * Finds the start and end period for the given rates and symbol properties.
     *
     * @param newRates the new rates
     * @param symbolProperties the symbol properties
     * @return a pair containing the start and end period
     */
    private Pair<Instant, Instant> findStartAndEndPeriod(List<Rate> newRates, SymbolProperties symbolProperties) {

        final var endDate = newRates.get(0).dateTime();
        final var startDate = symbolProperties.timeWindow().findStart(endDate);

        return Pair.of(startDate, endDate);
    }

    /**
     * Verifies and locks the given symbol. Ingestion should stop in case that lock cannot be obtained.
     * This is to prevent multiple ingestion processes for the same symbol.
     *
     * @param symbol the symbol to verify and lock
     * @return the symbol properties
     * @throws FailedToObtainLockException if the symbol cannot be locked
     */
    private SymbolProperties verifyAndLockSymbol(String symbol) {
        final Optional<SymbolProperties> symbolProperties = symbolPropertiesRepository.findSymbolProperties(symbol);
        if (symbolProperties.isEmpty()) {
            throw new FailedToObtainLockException("Symbol %s is not supported.".formatted(symbol));
        }
        if (symbolProperties.get().locked()) {
            throw new FailedToObtainLockException("Symbol %s is already locked.".formatted(symbol));
        }

        symbolPropertiesRepository.lockSymbol(symbol)
                .orElseThrow(() -> new FailedToObtainLockException("Failed to lock the symbol %s".formatted(symbol)));

        return symbolProperties.get();
    }

}