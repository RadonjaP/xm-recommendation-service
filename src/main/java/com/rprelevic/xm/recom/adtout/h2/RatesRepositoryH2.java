package com.rprelevic.xm.recom.adtout.h2;

import com.rprelevic.xm.recom.api.model.Rate;
import com.rprelevic.xm.recom.api.repository.RatesRepository;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class RatesRepositoryH2 implements RatesRepository {

    private static final String FIND_SYMBOL_IN_TIME_WINDOW_SQL = "SELECT * FROM rates WHERE symbol = ? AND date_time BETWEEN ? AND ?";
    private static final String SAVE_ALL_SQL = "INSERT INTO rates (symbol, date_time, rate) VALUES (?, ?, ?)";
    private static final String FIND_ALL_PRICES_FOR_DATE_SQL = "SELECT * FROM rates WHERE date_time BETWEEN ? AND ?";

    private final JdbcTemplate jdbcTemplate;

    public RatesRepositoryH2(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveAll(List<Rate> rates) {
        jdbcTemplate.batchUpdate(SAVE_ALL_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Rate rate = rates.get(i);
                ps.setString(1, rate.symbol());
                ps.setTimestamp(2, java.sql.Timestamp.from(rate.dateTime()));
                ps.setBigDecimal(3, BigDecimal.valueOf(rate.rate()));
            }

            @Override
            public int getBatchSize() {
                return rates.size();
            }
        });
    }

    @Override
    public List<Rate> findRatesBySymbolAndInTimeWindow(String symbol, java.time.Instant start, java.time.Instant end) {
        return jdbcTemplate.query(FIND_SYMBOL_IN_TIME_WINDOW_SQL,
                (rs, rowNum) -> new Rate(
                        rs.getTimestamp("date_time").toInstant(),
                        rs.getString("symbol"),
                        rs.getDouble("rate")
                ),
                symbol, java.sql.Timestamp.from(start), java.sql.Timestamp.from(end));
    }

    @Override
    public List<Rate> findAllPricesForDate(LocalDate date) {
        final LocalDateTime startOfDay = date.atStartOfDay();
        final LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return jdbcTemplate.query(FIND_ALL_PRICES_FOR_DATE_SQL,
                (rs, rowNum) -> new Rate(
                        rs.getTimestamp("date_time").toInstant(),
                        rs.getString("symbol"),
                        rs.getDouble("rate")
                ),
                java.sql.Timestamp.from(startOfDay.toInstant(ZoneOffset.UTC)),
                java.sql.Timestamp.from(endOfDay.toInstant(ZoneOffset.UTC)));
    }
}