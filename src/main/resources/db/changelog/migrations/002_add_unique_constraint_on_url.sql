--liquibase formatted sql

--changeset roadmap:002
ALTER TABLE url_mappings
    ADD CONSTRAINT uq_url_mappings_url UNIQUE (url);

--rollback ALTER TABLE url_mappings DROP CONSTRAINT uq_url_mappings_url;