DROP SCHEMA IF EXISTS public CASCADE;

CREATE SCHEMA IF NOT EXISTS google_drive_api;

SET SCHEMA 'google_drive_api';

CREATE TABLE users(
    id BIGSERIAL,
    first_name VARCHAR(50),
    last_name VARCHAR(50)
);
