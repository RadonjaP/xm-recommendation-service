package com.rprelevic.xm.recom.adtout.h2;

import com.rprelevic.xm.recom.api.model.Rate;
import com.rprelevic.xm.recom.api.repository.RatesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static com.rprelevic.xm.recom.utils.InstantUtils.toInstant;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ExtendWith(SpringExtension.class)
@ComponentScan(basePackages = "com.rprelevic.xm.recom.adtout.h2")
class RatesRepositoryH2Test {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RatesRepository repository;

    @BeforeEach
    void setUp() {
        repository = new RatesRepositoryH2(jdbcTemplate);
    }

    @Test
    void givenRatesList_whenRatesExist_thenExpectAllRatesStored() {
        List<Rate> rates = List.of(
                new Rate(Instant.now(), "BTC", 50000.0),
                new Rate(Instant.now(), "ETH", 4000.0)
        );

        repository.saveAll(rates);

        List<Rate> storedRates = jdbcTemplate.query("SELECT * FROM rates", (rs, rowNum) -> new Rate(
                toInstant(rs.getTimestamp("date_time")),
                rs.getString("symbol"),
                rs.getDouble("rate")
        ));
        assertThat(storedRates).hasSize(2);
    }

    @Test
    void givenRateExist_whenFindRatesInWindow_thenReturnRatesInTimeWindow() {
        Instant now = Instant.now();
        Rate rate = new Rate(now, "BTC", 50000.0);
        repository.saveAll(List.of(rate));

        List<Rate> foundRates = repository.findRatesBySymbolAndInTimeWindow("BTC", now.minusSeconds(60), now.plusSeconds(60));
        assertThat(foundRates).hasSize(1);
        assertThat(foundRates.get(0).symbol()).isEqualTo("BTC");
    }

    @Test
    void givenRateExist_whenFindRatesNotInWindow_thenReturnEmptyList() {
        Instant now = Instant.now();
        Rate rate = new Rate(now, "BTC", 50000.0);
        repository.saveAll(List.of(rate));

        List<Rate> foundRates = repository.findRatesBySymbolAndInTimeWindow("BTC", now.minusSeconds(120), now.minusSeconds(60));
        assertThat(foundRates).isEmpty();
    }

    @Test
    void givenRatesDoNotExist_whenFindRates_thenReturnEmptyList() {
        List<Rate> foundRates = repository.findRatesBySymbolAndInTimeWindow("BTC", Instant.now().minusSeconds(60), Instant.now().plusSeconds(60));
        assertThat(foundRates).isEmpty();
    }

    @Test
    void givenRatesInWindowExist_whenSymbolNotMatch_thenReturnEmptyList() {
        Instant now = Instant.now();
        Rate rate = new Rate(now, "BTC", 50000.0);
        repository.saveAll(List.of(rate));

        List<Rate> foundRates = repository.findRatesBySymbolAndInTimeWindow("ETH", now.minusSeconds(60), now.plusSeconds(60));
        assertThat(foundRates).isEmpty();
    }

    @Test
    void givenRatesInDatabase_whenFindAllPricesForDate_thenReturnRatesForThatDate() {
        // Given
        LocalDate date = LocalDate.of(2023, 1, 1);
        Rate rate1 = new Rate(toInstant(date.atStartOfDay()), "BTC", 50000.0);
        Rate rate2 = new Rate(toInstant(date.atTime(23, 59, 59)), "ETH", 4000.0);
        repository.saveAll(List.of(rate1, rate2));

        // When
        List<Rate> foundRates = repository.findAllPricesForDate(date);

        // Then
        assertThat(foundRates).hasSize(2);
        assertThat(foundRates).extracting(Rate::symbol).containsExactlyInAnyOrder("BTC", "ETH");
        assertThat(foundRates).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(rate1, rate2);
    }

}
