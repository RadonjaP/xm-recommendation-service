package com.rprelevic.xm.recom.adtout.h2;

import com.rprelevic.xm.recom.api.model.IngestionDetails;
import com.rprelevic.xm.recom.api.repository.IngestionDetailsRepository;
import com.rprelevic.xm.recom.impl.SourceType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.rprelevic.xm.recom.api.model.IngestionDetails.IngestionStatus.FAILURE;
import static com.rprelevic.xm.recom.api.model.IngestionDetails.IngestionStatus.SUCCESS;

public class IngestionDetailsRepositoryH2 implements IngestionDetailsRepository {

    private static final String SAVE_QUERY = """
                MERGE INTO ingestion_details (ingestion_id, source_name, symbol, source_type, ingestion_start_time, ingestion_end_time, number_of_records_ingested, status)
                KEY (ingestion_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    public IngestionDetailsRepositoryH2(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(IngestionDetails ingestionDetails) {

        jdbcTemplate.update(SAVE_QUERY, ps -> {
            ps.setString(1, ingestionDetails.ingestionId());
            ps.setString(2, ingestionDetails.sourceName());
            ps.setString(3, ingestionDetails.symbol());
            ps.setString(4, ingestionDetails.sourceType().name());
            ps.setObject(5, ingestionDetails.ingestionStartTime());
            ps.setObject(6, ingestionDetails.ingestionEndTime());
            ps.setInt(7, ingestionDetails.numberOfRecordsIngested());
            ps.setString(8, ingestionDetails.status().name());
        });
    }

    @Override
    public void ingestionFailed(IngestionDetails ingestionDetails) {

        final var updatedDetails = ingestionDetails.toBuilder()
                .ingestionEndTime(LocalDateTime.now())
                .status(FAILURE)
                .numberOfRecordsIngested(0)
                .build();

        save(updatedDetails);
    }

    @Override
    public void ingestionSuccessful(IngestionDetails ingestionDetails, int numberOfRecordsIngested) {

        final var updatedDetails = ingestionDetails.toBuilder()
                .ingestionEndTime(LocalDateTime.now())
                .status(SUCCESS)
                .numberOfRecordsIngested(numberOfRecordsIngested)
                .build();

        save(updatedDetails);
    }

    @Override
    public Optional<IngestionDetails> findByIngestionId(String ingestionId) {

        return jdbcTemplate.query(
                "SELECT * FROM ingestion_details WHERE ingestion_id = ?",
                rs -> {
                    if (rs.next()) {
                        return Optional.of(new IngestionDetails(
                                rs.getString("ingestion_id"),
                                rs.getString("source_name"),
                                rs.getString("symbol"),
                                SourceType.valueOf(rs.getString("source_type")),
                                rs.getTimestamp("ingestion_start_time").toLocalDateTime(),
                                toNullableLocalDateTime(rs.getTimestamp("ingestion_end_time")),
                                rs.getInt("number_of_records_ingested"),
                                IngestionDetails.IngestionStatus.valueOf(rs.getString("status"))
                        ));
                    } else {
                        return Optional.empty();
                    }
                },
                new Object[]{ingestionId}
        );
    }

    /**
     * Converts a nullable Timestamp to a nullable LocalDateTime.
     *
     * @param timestamp the nullable Timestamp
     * @return the nullable LocalDateTime
     */
    private LocalDateTime toNullableLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

}
