package com.rprelevic.xm.recom.adtout.h2;

import com.rprelevic.xm.recom.api.model.Price;
import com.rprelevic.xm.recom.api.repository.CryptoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ExtendWith(SpringExtension.class)
@ComponentScan(basePackages = "com.rprelevic.xm.recom.adtout.h2")
class CryptoRepositoryH2Test {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private CryptoRepository repository;

    @BeforeEach
    void setUp() {
        repository = new CryptoRepositoryH2(jdbcTemplate);
    }

    @Test
    void givenPricesList_whenPricesExist_thenExpectAllPricesStored() {
        List<Price> prices = List.of(
                new Price(Instant.now(), "BTC", 50000.0),
                new Price(Instant.now(), "ETH", 4000.0)
        );

        repository.saveAll(prices);

        List<Price> storedPrices = jdbcTemplate.query("SELECT * FROM price", (rs, rowNum) -> new Price(
                rs.getTimestamp("date_time").toInstant(),
                rs.getString("symbol"),
                rs.getDouble("rate")
        ));
        assertThat(storedPrices).hasSize(2);
    }

    @Test
    void givenPriceExist_whenFindPricesInWindow_thenReturnPricesInTimeWindow() {
        Instant now = Instant.now();
        Price price = new Price(now, "BTC", 50000.0);
        repository.saveAll(List.of(price));

        List<Price> foundPrices = repository.findPricesBySymbolAndInTimeWindow("BTC", now.minusSeconds(60), now.plusSeconds(60));
        assertThat(foundPrices).hasSize(1);
        assertThat(foundPrices.get(0).symbol()).isEqualTo("BTC");
    }

    @Test
    void givenPriceExist_whenFindPricesNotInWindow_thenReturnEmptyList() {
        Instant now = Instant.now();
        Price price = new Price(now, "BTC", 50000.0);
        repository.saveAll(List.of(price));

        List<Price> foundPrices = repository.findPricesBySymbolAndInTimeWindow("BTC", now.minusSeconds(120), now.minusSeconds(60));
        assertThat(foundPrices).isEmpty();
    }

    @Test
    void givenPricesDoNotExist_whenFindPrices_thenReturnEmptyList() {
        List<Price> foundPrices = repository.findPricesBySymbolAndInTimeWindow("BTC", Instant.now().minusSeconds(60), Instant.now().plusSeconds(60));
        assertThat(foundPrices).isEmpty();
    }

    @Test
    void givenPricesInWindowExist_whenSymbolNotMatch_thenReturnEmptyList() {
        Instant now = Instant.now();
        Price price = new Price(now, "BTC", 50000.0);
        repository.saveAll(List.of(price));

        List<Price> foundPrices = repository.findPricesBySymbolAndInTimeWindow("ETH", now.minusSeconds(60), now.plusSeconds(60));
        assertThat(foundPrices).isEmpty();
    }

}
