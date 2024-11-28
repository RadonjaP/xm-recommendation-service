package com.rprelevic.xm.recom.api.repository;

import com.rprelevic.xm.recom.api.model.IngestionDetails;

import java.util.Optional;

public interface IngestionDetailsRepository {

    void save(IngestionDetails ingestionDetails);
    void ingestionFailed(IngestionDetails ingestionDetails);
    void ingestionSuccessful(IngestionDetails ingestionDetails, int numberOfRecordsIngested);
    Optional<IngestionDetails> findByIngestionId(String ingestionId);

}
