CREATE TABLE IF NOT EXISTS case_document
(
  id                    BIGSERIAL PRIMARY KEY,
  case_uuid             UUID      NOT NULL,
  document_uuid         UUID      NOT NULL,
  document_display_name TEXT      NOT NULL,
  document_type         TEXT      NOT NULL,
  timestamp             TIMESTAMP NOT NULL,
  s3_orig_link          TEXT      NOT NULL,
  s3_pdf_link           TEXT      NOT NULL,
  status                TEXT      NOT NULL,
  deleted               BOOLEAN   NOT NULL,

  CONSTRAINT document_uuid_idempotent UNIQUE (document_uuid),

  CONSTRAINT fk_stage_id FOREIGN KEY (case_uuid) REFERENCES case_data (uuid)

);

CREATE INDEX idx_document_uuid
  ON case_document (document_uuid);



