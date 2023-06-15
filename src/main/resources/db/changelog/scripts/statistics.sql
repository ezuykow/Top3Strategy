-- liquibase formatted sql

-- changeset ezuykow:1
CREATE TABLE statistics
(
        id SERIAL PRIMARY KEY
);

-- changeset ezuykow:2
ALTER TABLE statistics
        ADD COLUMN bets_count INT DEFAULT 0;
ALTER TABLE statistics
        ADD COLUMN looses INT DEFAULT 0;
ALTER TABLE statistics
        ADD COLUMN bank_status INT DEFAULT 0;