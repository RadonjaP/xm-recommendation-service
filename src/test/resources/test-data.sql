DROP TABLE IF EXISTS symbol_properties;

CREATE TABLE symbol_properties (
    symbol VARCHAR(10) PRIMARY KEY,
    time_window VARCHAR(20),
    locked BOOLEAN,
    locked_at TIMESTAMP
);

INSERT INTO symbol_properties (symbol, time_window, locked, locked_at) VALUES
('BTC', 'MONTHLY', false, CURRENT_TIMESTAMP),
('ETC', 'MONTHLY', true, CURRENT_TIMESTAMP),
('CRO', 'MONTHLY', false, CURRENT_TIMESTAMP),
('VVS', 'MONTHLY', true, CURRENT_TIMESTAMP);