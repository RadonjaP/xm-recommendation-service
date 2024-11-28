package com.rprelevic.xm.recom.adtout.h2;

import com.rprelevic.xm.recom.api.model.Price;
import com.rprelevic.xm.recom.api.repository.CryptoRepository;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class CryptoRepositoryH2 implements CryptoRepository {

    private static final String FIND_SYMBOL_IN_TIME_WINDOW_SQL = "SELECT * FROM price WHERE symbol = ? AND date_time BETWEEN ? AND ?";
    private static final String SAVE_ALL_SQL = "INSERT INTO price (symbol, date_time, rate) VALUES (?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    public CryptoRepositoryH2(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveAll(List<Price> prices) {
        jdbcTemplate.batchUpdate(SAVE_ALL_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Price price = prices.get(i);
                ps.setString(1, price.symbol());
                ps.setTimestamp(2, java.sql.Timestamp.from(price.dateTime()));
                ps.setBigDecimal(3, BigDecimal.valueOf(price.rate()));
            }

            @Override
            public int getBatchSize() {
                return prices.size();
            }
        });
    }

    @Override
    public List<Price> findPricesBySymbolAndInTimeWindow(String symbol, java.time.Instant start, java.time.Instant end) {
        return jdbcTemplate.query(FIND_SYMBOL_IN_TIME_WINDOW_SQL,
                (rs, rowNum) -> new Price(
                        rs.getTimestamp("date_time").toInstant(),
                        rs.getString("symbol"),
                        rs.getDouble("rate")
                ),
                symbol, java.sql.Timestamp.from(start), java.sql.Timestamp.from(end));
    }
}