CREATE SEQUENCE case_ref
  START 0120001;

DROP TABLE IF EXISTS case_data;

CREATE TABLE IF NOT EXISTS case_data
(
  id        BIGSERIAL PRIMARY KEY,
  uuid      UUID      NOT NULL,
  type      TEXT      NOT NULL,
  reference TEXT      NOT NULL,
  created   TIMESTAMP NOT NULL,
  updated   TIMESTAMP,

  CONSTRAINT case_ref_idempotent UNIQUE (reference),
  CONSTRAINT case_uuid_idempotent UNIQUE (uuid)
);