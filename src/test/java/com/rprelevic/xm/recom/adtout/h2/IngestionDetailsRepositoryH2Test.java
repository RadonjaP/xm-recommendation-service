package com.rprelevic.xm.recom.adtout.h2;

import com.rprelevic.xm.recom.api.model.IngestionDetails;
import com.rprelevic.xm.recom.api.repository.IngestionDetailsRepository;
import com.rprelevic.xm.recom.impl.SourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.rprelevic.xm.recom.api.model.IngestionDetails.IngestionStatus.*;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@ExtendWith(SpringExtension.class)
@ComponentScan(basePackages = "com.rprelevic.xm.recom.adtout.h2")
class IngestionDetailsRepositoryH2Test {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private IngestionDetailsRepository repository;

    @BeforeEach
    void setUp() {
        repository = new IngestionDetailsRepositoryH2(jdbcTemplate);
    }

    @Test
    void givenIngestionDetails_whenIngestionRunning_thenExpectStatusInProgress() {
        String ingestionId = UUID.randomUUID().toString();
        IngestionDetails details = IngestionDetails.builder()
                .ingestionId(ingestionId)
                .sourceName(SourceType.JSON.name())
                .symbol("BTC")
                .sourceType(SourceType.API)
                .ingestionStartTime(LocalDateTime.now())
                .status(IN_PROGRESS)
                .build();
        repository.save(details);

        Optional<IngestionDetails> found = repository.findByIngestionId(ingestionId);
        assertTrue(found.isPresent());
        assertEquals(details.toBuilder().status(IN_PROGRESS).numberOfRecordsIngested(0)
                .ingestionStartTime(found.get().ingestionStartTime())
                .build(), found.get());
    }

    @Test
    void givenIngestionFailed_whenFindIngestion_thenExpectStatusFailure() {
        String ingestionId = UUID.randomUUID().toString();
        IngestionDetails details = IngestionDetails.builder()
                .ingestionId(ingestionId)
                .sourceName(SourceType.JSON.name())
                .symbol("BTC")
                .sourceType(SourceType.API)
                .ingestionStartTime(LocalDateTime.now())
                .status(IN_PROGRESS)
                .build();
        repository.save(details);
        repository.ingestionFailed(details);

        Optional<IngestionDetails> found = repository.findByIngestionId(ingestionId);
        assertTrue(found.isPresent());

        assertEquals(details.toBuilder().status(FAILURE).numberOfRecordsIngested(0)
                .ingestionEndTime(found.get().ingestionEndTime())
                .ingestionStartTime(found.get().ingestionStartTime())
                .build(), found.get());
        assertNotNull(found.get().ingestionEndTime());
    }

    @Test
    void givenIngestionCompleted_whenFindIngestion_thenExpectStatusSuccess() {
        String ingestionId = UUID.randomUUID().toString();
        IngestionDetails details = IngestionDetails.builder()
                .ingestionId(ingestionId)
                .sourceName(SourceType.JSON.name())
                .symbol("BTC")
                .sourceType(SourceType.API)
                .ingestionStartTime(LocalDateTime.now())
                .status(IN_PROGRESS)
                .build();
        repository.save(details);
        repository.ingestionSuccessful(details, 100);

        Optional<IngestionDetails> found = repository.findByIngestionId(ingestionId);
        assertTrue(found.isPresent());
        assertEquals(details.toBuilder().status(SUCCESS).numberOfRecordsIngested(100)
                .ingestionEndTime(found.get().ingestionEndTime())
                .ingestionStartTime(found.get().ingestionStartTime())
                .build(), found.get());
    }

    @Test
    void givenIngestionDoesNotExist_whenFindIngestion_thenExpectEmptyOptional() {
        String ingestionId = UUID.randomUUID().toString();
        Optional<IngestionDetails> found = repository.findByIngestionId(ingestionId);
        assertTrue(found.isEmpty());
    }

}