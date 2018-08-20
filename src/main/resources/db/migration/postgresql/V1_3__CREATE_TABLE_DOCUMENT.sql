DROP TABLE IF EXISTS document_data cascade;

CREATE TABLE IF NOT EXISTS document_data
(
  id           BIGSERIAL PRIMARY KEY,
  uuid         UUID      NOT NULL,
  type         TEXT      NOT NULL,
  case_uuid    UUID      NOT NULL,
  created      TIMESTAMP NOT NULL,
  updated      TIMESTAMP,
  display_name TEXT      NOT NULL,
  orig_link    TEXT,
  pdf_link     TEXT,
  status       TEXT      NOT NULL,
  deleted      BOOLEAN   NOT NULL DEFAULT FALSE,

  CONSTRAINT document_uuid_idempotent UNIQUE (uuid),
  CONSTRAINT fk_document_id FOREIGN KEY (case_uuid) REFERENCES case_data (uuid)
);