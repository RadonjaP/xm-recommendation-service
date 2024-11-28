package com.rprelevic.xm.recom.adtout.h2;

import com.rprelevic.xm.recom.api.model.SymbolProperties;
import com.rprelevic.xm.recom.api.repository.SymbolPropertiesRepository;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

public class SymbolPropertiesRepositoryH2 implements SymbolPropertiesRepository {

    private static final String FETCH_SYMBOL_PROPERTIES_SQL = "SELECT symbol, time_window, locked, locked_at FROM symbol_properties WHERE symbol = ?";
    private static final String LOCK_SYMBOL_SQL = "UPDATE symbol_properties SET locked = true, locked_at = CURRENT_TIMESTAMP WHERE symbol = ? AND locked = false";
    private static final String UNLOCK_SYMBOL_SQL = "UPDATE symbol_properties SET locked = false WHERE symbol = ? AND locked = true";

    private final JdbcTemplate jdbcTemplate;

    public SymbolPropertiesRepositoryH2(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<SymbolProperties> findSymbolProperties(String symbol) {

        return jdbcTemplate.query(FETCH_SYMBOL_PROPERTIES_SQL, (rs) -> {
            if (rs.next()) {
                return Optional.of(new SymbolProperties(
                        rs.getString("symbol"),
                        SymbolProperties.TimeWindow.valueOf(rs.getString("time_window")),
                        rs.getBoolean("locked"),
                        rs.getTimestamp("locked_at").toLocalDateTime()
                ));
            }
            return Optional.empty();
        }, new Object[]{symbol});
    }

    @Override
    public Optional<Boolean> lockSymbol(String symbol) {

        int rowsAffected = jdbcTemplate.update(LOCK_SYMBOL_SQL, ps -> ps.setString(1, symbol));
        if (rowsAffected > 0) {
            return Optional.of(true);
        }

        return Optional.of(false);
    }

    @Override
    public Optional<Boolean> unlockSymbol(String symbol) {

        int rowsAffected = jdbcTemplate.update(UNLOCK_SYMBOL_SQL, ps -> ps.setString(1, symbol));
        if (rowsAffected > 0) {
            return Optional.of(true);
        }

        return Optional.of(false);
    }
}
