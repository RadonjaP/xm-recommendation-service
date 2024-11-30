package com.rprelevic.xm.recom.adtout.h2;

import com.rprelevic.xm.recom.api.model.CryptoStats;
import com.rprelevic.xm.recom.api.model.DataStatus;
import com.rprelevic.xm.recom.api.repository.CryptoStatsRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class CryptoStatsRepositoryH2 implements CryptoStatsRepository {

    private static final String MERGE_CRYPTO_STATS_SQL = """
                MERGE INTO crypto_stats (symbol, period_start, period_end, status, min_rate, max_rate, oldest_rate, latest_rate, normalized_range)
                KEY (symbol, period_start)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String FIND_LATEST_STATS_FOR_ALL_SYMBOLS = """
                SELECT cs1.*
                FROM crypto_stats cs1
                INNER JOIN (
                    SELECT symbol, MAX(period_end) AS latest_period_end
                    FROM crypto_stats
                    GROUP BY symbol
                ) cs2 ON cs1.symbol = cs2.symbol AND cs1.period_end = cs2.latest_period_end
            """;
    private static final String FIND_LATEST_CRYPTO_STATS_BY_SYMBOL_SQL = """ 
            SELECT * FROM crypto_stats
            WHERE symbol = ?
            ORDER BY period_start DESC LIMIT 1
            """;

    private final JdbcTemplate jdbcTemplate;

    public CryptoStatsRepositoryH2(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @CacheEvict(value = {"cryptoStats", "cryptoStatsForSymbol", "highestNormalizedRangeForDay"}, allEntries = true)
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
    public Optional<CryptoStats> findLatestCryptoStatsBySymbol(String symbol) {
        return jdbcTemplate.query(FIND_LATEST_CRYPTO_STATS_BY_SYMBOL_SQL, (rs) -> {
            if (rs.next()) {
                return Optional.of(new CryptoStats(
                        rs.getTimestamp("period_start").toLocalDateTime(),
                        rs.getTimestamp("period_end").toLocalDateTime(),
                        rs.getString("symbol"),
                        DataStatus.valueOf(rs.getString("status")),
                        rs.getDouble("min_rate"),
                        rs.getDouble("max_rate"),
                        rs.getDouble("oldest_rate"),
                        rs.getDouble("latest_rate"),
                        rs.getDouble("normalized_range")
                ));
            }
            return Optional.empty();
        }, symbol);
    }

    @Override
    public List<CryptoStats> findLatestStatsForAllSymbols() {
        return jdbcTemplate.query(FIND_LATEST_STATS_FOR_ALL_SYMBOLS, (rs, rowNum) -> new CryptoStats(
                rs.getObject("period_start", LocalDateTime.class),
                rs.getObject("period_end", LocalDateTime.class),
                rs.getString("symbol"),
                DataStatus.valueOf(rs.getString("status")),
                rs.getDouble("min_rate"),
                rs.getDouble("max_rate"),
                rs.getDouble("oldest_rate"),
                rs.getDouble("latest_rate"),
                rs.getDouble("normalized_range")
        ));
    }
}