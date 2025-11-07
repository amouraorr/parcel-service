-- V1: create parcels table

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS parcels (

    id BIGSERIAL PRIMARY KEY,
    resident_name VARCHAR(255) NOT NULL,
    apartment VARCHAR(50) NOT NULL,
    description TEXT,
    contact VARCHAR(255),
    channel VARCHAR(20),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);