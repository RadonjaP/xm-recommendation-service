CREATE TABLE IF NOT EXISTS crypto_stats (
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    symbol VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    min_rate DOUBLE NOT NULL,
    max_rate DOUBLE NOT NULL,
    oldest_price DOUBLE NOT NULL,
    latest_price DOUBLE NOT NULL,
    normalized_range DOUBLE NOT NULL,
    PRIMARY KEY (symbol, period_start)
);

CREATE TABLE IF NOT EXISTS ingestion_details (
    ingestion_id VARCHAR(36) NOT NULL,
    source_name VARCHAR(255) NOT NULL,
    symbol VARCHAR(255) NOT NULL,
    source_type VARCHAR(255) NOT NULL,
    ingestion_start_time TIMESTAMP NOT NULL,
    ingestion_end_time TIMESTAMP,
    number_of_records_ingested INT NOT NULL,
    status VARCHAR(255) NOT NULL,
    PRIMARY KEY (ingestion_id)
);

CREATE TABLE IF NOT EXISTS price (
    date_time TIMESTAMP NOT NULL,
    symbol VARCHAR(255) NOT NULL,
    rate DOUBLE NOT NULL,
    PRIMARY KEY (date_time, symbol)
);

CREATE TABLE IF NOT EXISTS symbol_properties (
    symbol VARCHAR(255) NOT NULL,
    time_window VARCHAR(255) NOT NULL,
    locked BOOLEAN NOT NULL,
    locked_at TIMESTAMP,
    PRIMARY KEY (symbol)
);