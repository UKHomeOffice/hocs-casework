DROP TABLE IF EXISTS document_data cascade;

CREATE TABLE IF NOT EXISTS document_data
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

  CONSTRAINT fk_document_id FOREIGN KEY (case_uuid) REFERENCES case_data (uuid)
);

CREATE INDEX idx_document_document_uuid  ON document_data (document_uuid);
CREATE INDEX idx_document_case_uuid  ON document_data (case_uuid);
CREATE INDEX idx_document_document_type ON document_data (document_type);
CREATE INDEX idx_document_deleted  ON document_data (deleted);

DROP TABLE IF EXISTS audit_document_data;

CREATE TABLE IF NOT EXISTS audit_document_data
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
  deleted               BOOLEAN   NOT NULL
);

CREATE INDEX idx_audit_document_document_uuid ON audit_document_data (document_uuid);
CREATE INDEX idx_audit_document_case_uuid ON audit_document_data (case_uuid);
CREATE INDEX idx_audit_document_timestamp ON audit_document_data (timestamp);