package com.rprelevic.xm.recom.api.repository;

import com.rprelevic.xm.recom.api.model.IngestionDetails;

import java.util.Optional;

/**
 * Repository interface for managing {@link IngestionDetails} entities.
 */
public interface IngestionDetailsRepository {

    /**
     * Saves the given {@link IngestionDetails} entity.
     *
     * @param ingestionDetails the {@link IngestionDetails} entity to save
     */
    void save(IngestionDetails ingestionDetails);

    /**
     * Marks the given {@link IngestionDetails} entity as failed.
     *
     * @param ingestionDetails the {@link IngestionDetails} entity to mark as failed
     */
    void ingestionFailed(IngestionDetails ingestionDetails);

    /**
     * Marks the given {@link IngestionDetails} entity as successful and updates the number of records ingested.
     *
     * @param ingestionDetails the {@link IngestionDetails} entity to mark as successful
     * @param numberOfRecordsIngested the number of records ingested
     */
    void ingestionSuccessful(IngestionDetails ingestionDetails, int numberOfRecordsIngested);

    /**
     * Finds an {@link IngestionDetails} entity by its ingestion ID.
     *
     * @param ingestionId the ingestion ID to search for
     * @return an {@link Optional} containing the found {@link IngestionDetails} entity, or empty if not found
     */
    Optional<IngestionDetails> findByIngestionId(String ingestionId);

}