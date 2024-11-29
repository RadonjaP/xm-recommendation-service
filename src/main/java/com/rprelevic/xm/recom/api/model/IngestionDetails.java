package com.rprelevic.xm.recom.api.model;

import com.rprelevic.xm.recom.impl.SourceType;

import java.time.LocalDateTime;

/**
 * Represents the details of an ingestion process.
 */
public record IngestionDetails(
        String ingestionId,
        String sourceName,
        String symbol,
        SourceType sourceType,
        LocalDateTime ingestionStartTime,
        LocalDateTime ingestionEndTime,
        int numberOfRecordsIngested,
        IngestionStatus status
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String ingestionId;
        private String sourceName;
        private String symbol;
        private SourceType sourceType;
        private LocalDateTime ingestionStartTime;
        private LocalDateTime ingestionEndTime;
        private int numberOfRecordsIngested;
        private IngestionStatus status;

        public Builder ingestionId(String ingestionId) {
            this.ingestionId = ingestionId;
            return this;
        }

        public Builder sourceName(String sourceName) {
            this.sourceName = sourceName;
            return this;
        }

        public Builder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Builder sourceType(SourceType sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        public Builder ingestionStartTime(LocalDateTime ingestionStartTime) {
            this.ingestionStartTime = ingestionStartTime;
            return this;
        }

        public Builder ingestionEndTime(LocalDateTime ingestionEndTime) {
            this.ingestionEndTime = ingestionEndTime;
            return this;
        }

        public Builder numberOfRecordsIngested(int numberOfRecordsIngested) {
            this.numberOfRecordsIngested = numberOfRecordsIngested;
            return this;
        }

        public Builder status(IngestionStatus status) {
            this.status = status;
            return this;
        }

        public IngestionDetails build() {
            return new IngestionDetails(ingestionId, sourceName, symbol, sourceType, ingestionStartTime, ingestionEndTime, numberOfRecordsIngested, status);
        }
    }

    public Builder toBuilder() {
        return new Builder()
                .ingestionId(this.ingestionId)
                .sourceName(this.sourceName)
                .symbol(this.symbol)
                .sourceType(this.sourceType)
                .ingestionStartTime(this.ingestionStartTime)
                .ingestionEndTime(this.ingestionEndTime)
                .numberOfRecordsIngested(this.numberOfRecordsIngested)
                .status(this.status);
    }

    public enum IngestionStatus {
        IN_PROGRESS, SUCCESS, FAILURE
    }


}
