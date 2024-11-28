package com.rprelevic.xm.recom.adtout.h2;

import com.rprelevic.xm.recom.api.model.SymbolProperties;
import com.rprelevic.xm.recom.api.repository.SymbolPropertiesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@ExtendWith(SpringExtension.class)
@ComponentScan(basePackages = "com.rprelevic.xm.recom.adtout.h2")
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class SymbolPropertiesRepositoryH2Test {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private SymbolPropertiesRepository repository;

    @BeforeEach
    void setUp() {
        repository = new SymbolPropertiesRepositoryH2(jdbcTemplate);
    }

    @Test
    void givenSymbolExists_whenFetchProperties_thenExpectData() {
        Optional<SymbolProperties> properties = repository.findSymbolProperties("BTC");
        assertTrue(properties.isPresent());
        assertEquals("BTC", properties.get().symbol());
    }

    @Test
    void givenSymbolNotExists_whenFetchProperties_thenExpectNoData() {
        Optional<SymbolProperties> properties = repository.findSymbolProperties("NNS");
        assertFalse(properties.isPresent());
    }

    @Test
    void givenSymbolNotLocked_whenLock_expectTrue() {
        Optional<Boolean> result = repository.lockSymbol("BTC");
        assertTrue(result.isPresent());
        assertTrue(result.get());

        Optional<SymbolProperties> properties = repository.findSymbolProperties("BTC");
        assertTrue(properties.isPresent());
        assertTrue(properties.get().locked());
        assertNotNull(properties.get().lockedAt());
        assertTrue(properties.get().lockedAt().isAfter(LocalDateTime.now().minusSeconds(5)));
    }

    @Test
    void givenSymbolLocked_whenLock_expectFalse() {
        final var res = repository.lockSymbol("ETC");
        final var prop = repository.findSymbolProperties("ETC");
        Optional<Boolean> result = repository.lockSymbol("ETC");
        assertTrue(result.isPresent());
        assertFalse(result.get());
    }

    @Test
    void givenSymbolNotLocked_whenUnlock_expectFalse() {
        repository.unlockSymbol("CRO");
        Optional<Boolean> result = repository.unlockSymbol("CRO");
        assertTrue(result.isPresent());
        assertFalse(result.get());
    }

    @Test
    void givenSymbolLocked_whenUnlock_expectTrue() {
        repository.lockSymbol("VVS");
        Optional<Boolean> unlocked = repository.unlockSymbol("VVS");
        assertTrue(unlocked.isPresent());
        assertTrue(unlocked.get());
    }

    @Test
    void givenSymbolNotExists_whenLock_expectFalse() {
        Optional<Boolean> locked = repository.lockSymbol("TEST");
        assertTrue(locked.isPresent());
        assertFalse(locked.get());
    }

    @Test
    void givenSymbolNotExists_whenUnlock_expectFalse() {
        Optional<Boolean> unlocked = repository.unlockSymbol("TEST");
        assertTrue(unlocked.isPresent());
        assertFalse(unlocked.get());
    }
}