package com.rprelevic.xm.recom.adtout.h2;

import com.rprelevic.xm.recom.api.model.CryptoStats;
import com.rprelevic.xm.recom.api.model.DataStatus;
import com.rprelevic.xm.recom.api.repository.CryptoStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ExtendWith(SpringExtension.class)
@ComponentScan(basePackages = "com.rprelevic.xm.recom.adtout.h2")
class CryptoStatsRepositoryH2Test {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private CryptoStatsRepository repository;

    @BeforeEach
    void setUp() {
        repository = new CryptoStatsRepositoryH2(jdbcTemplate);
    }

    @Test
    void givenCryptoStats_whenSaveStats_thenStatsPersistedWithSameProperties() {
        CryptoStats cryptoStats = CryptoStats.of(
                "BTC", toLocalDateTime(2023, 1, 1), toLocalDateTime(2023, 1, 31),
                DataStatus.GREEN, 30000.0, 40000.0, 31000.0, 39000.0, 0.25
        );

        repository.saveCryptoStats(cryptoStats);

        Optional<CryptoStats> persistedStats = repository.findCryptoStatsBySymbolAndPeriodStart(
                cryptoStats.symbol(), cryptoStats.periodStart()
        );

        assertThat(persistedStats).isPresent();
        assertThat(persistedStats.get()).usingRecursiveComparison().isEqualTo(cryptoStats);
    }

    @Test
    void givenCryptoStatsExists_whenUpdateStats_thenExpectUpdatedStatsPersisted() {
        CryptoStats initialStats = CryptoStats.of(
                "BTC", toLocalDateTime(2023, 1, 1), toLocalDateTime(2023, 1, 31),
                DataStatus.GREEN, 30000.0, 40000.0, 31000.0, 39000.0, 0.25
        );
        repository.saveCryptoStats(initialStats);

        CryptoStats updatedStats = CryptoStats.of(
                "BTC", toLocalDateTime(2023, 1, 1), toLocalDateTime(2023, 1, 31),
                DataStatus.GREEN, 32000.0, 42000.0, 33000.0, 41000.0, 0.30
        );
        repository.saveCryptoStats(updatedStats);

        Optional<CryptoStats> persistedStats = repository.findCryptoStatsBySymbolAndPeriodStart(
                updatedStats.symbol(), updatedStats.periodStart()
        );

        assertThat(persistedStats).isPresent();
        assertThat(persistedStats.get()).usingRecursiveComparison().isEqualTo(updatedStats);
    }

    @Test
    void givenCryptoStatsExists_whenFindBySymbolAndPeriod_thenReturnProperStats() {
        CryptoStats cryptoStats = CryptoStats.of(
                "BTC", toLocalDateTime(2023, 1, 1),
                toLocalDateTime(2023, 1, 31),
                DataStatus.GREEN, 30000.0, 40000.0, 31000.0, 39000.0, 0.25
        );
        repository.saveCryptoStats(cryptoStats);

        Optional<CryptoStats> foundStats = repository.findCryptoStatsBySymbolAndPeriodStart(
                "BTC", toLocalDateTime(2023, 1, 1)
        );

        assertThat(foundStats).isPresent();
        assertThat(foundStats.get()).usingRecursiveComparison().isEqualTo(cryptoStats);
    }

    @Test
    void givenCryptoStatsExists_whenFindForNonExistingSymbol_thenReturnEmptyList() {
        CryptoStats cryptoStats = CryptoStats.of(
                "BTC", toLocalDateTime(2023, 1, 1),
                toLocalDateTime(2023, 1, 31),
                DataStatus.GREEN, 30000.0, 40000.0, 31000.0, 39000.0, 0.25
        );
        repository.saveCryptoStats(cryptoStats);

        Optional<CryptoStats> foundStats = repository.findCryptoStatsBySymbolAndPeriodStart(
                "ETH", toLocalDateTime(2023, 1, 1)
        );

        assertThat(foundStats).isNotPresent();
    }

    @Test
    void givenCryptoStatsExistsWithSymbol_whenFindForNonExistingStartPeriod_thenReturnEmptyList() {
        CryptoStats cryptoStats = CryptoStats.of(
                "BTC", toLocalDateTime(2023, 1, 1),
                toLocalDateTime(2023, 1, 31),
                DataStatus.GREEN, 30000.0, 40000.0, 31000.0, 39000.0, 0.25
        );
        repository.saveCryptoStats(cryptoStats);

        Optional<CryptoStats> foundStats = repository.findCryptoStatsBySymbolAndPeriodStart(
                "BTC", toLocalDateTime(2023, 2, 1)
        );

        assertThat(foundStats).isNotPresent();
    }

    private LocalDateTime toLocalDateTime(int year, int month, int day) {
        return LocalDate.of(year, month, day).atStartOfDay();
    }
}