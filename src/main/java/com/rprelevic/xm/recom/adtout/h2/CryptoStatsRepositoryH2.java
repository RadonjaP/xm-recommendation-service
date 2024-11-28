package com.rprelevic.xm.recom.adtout.h2;

import com.rprelevic.xm.recom.api.model.CryptoStats;
import com.rprelevic.xm.recom.api.model.DataStatus;
import com.rprelevic.xm.recom.api.repository.CryptoStatsRepository;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

public class CryptoStatsRepositoryH2 implements CryptoStatsRepository {

    private static final String MERGE_CRYPTO_STATS_SQL = """
                MERGE INTO crypto_stats (symbol, period_start, period_end, status, min_rate, max_rate, oldest_price, latest_price, normalized_range)
                KEY (symbol, period_start)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String FIND_STATS_BY_PRIMARY_KEY_SQL = "SELECT * FROM crypto_stats WHERE symbol = ? AND period_start = ?";

    private final JdbcTemplate jdbcTemplate;

    public CryptoStatsRepositoryH2(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveCryptoStats(CryptoStats cryptoStats) {
        jdbcTemplate.update(MERGE_CRYPTO_STATS_SQL, ps -> {
            ps.setString(1, cryptoStats.symbol());
            ps.setObject(2, cryptoStats.periodStart());
            ps.setObject(3, cryptoStats.periodEnd());
            ps.setString(4, cryptoStats.status().name());
            ps.setDouble(5, cryptoStats.minRate());
            ps.setDouble(6, cryptoStats.maxRate());
            ps.setDouble(7, cryptoStats.oldestRate());
            ps.setDouble(8, cryptoStats.latestRate());
            ps.setDouble(9, cryptoStats.normalizedRange());
        });
    }

    @Override
    public Optional<CryptoStats> findCryptoStatsBySymbolAndPeriodStart(String symbol, LocalDateTime periodStart) {
        return jdbcTemplate.query(
                FIND_STATS_BY_PRIMARY_KEY_SQL,
                rs -> {
                    if (rs.next()) {
                        return Optional.of(new CryptoStats(
                                rs.getObject("period_start", LocalDateTime.class),
                                rs.getObject("period_end", LocalDateTime.class),
                                rs.getString("symbol"),
                                DataStatus.valueOf(rs.getString("status")),
                                rs.getDouble("min_rate"),
                                rs.getDouble("max_rate"),
                                rs.getDouble("oldest_price"),
                                rs.getDouble("latest_price"),
                                rs.getDouble("normalized_range")
                        ));
                    } else {
                        return Optional.empty();
                    }
                },
                new Object[]{symbol, periodStart}
        );
    }
}