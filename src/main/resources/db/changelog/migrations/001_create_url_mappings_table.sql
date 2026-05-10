--liquibase formatted sql

--changeset roadmap:001
CREATE TABLE url_mappings
(
    id           BIGSERIAL PRIMARY KEY,
    url          TEXT        NOT NULL,
    short_code   VARCHAR(10) NOT NULL UNIQUE,
    access_count BIGINT      NOT NULL DEFAULT 0,
    created_at   TIMESTAMP   NOT NULL,
    updated_at   TIMESTAMP   NOT NULL
);

--rollback DROP TABLE url_mappings;