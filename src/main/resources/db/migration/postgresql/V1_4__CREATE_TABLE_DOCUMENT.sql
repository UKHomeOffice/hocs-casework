CREATE TABLE IF NOT EXISTS documentData
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

CREATE INDEX idx_document_document_uuid  ON documentData (document_uuid);
CREATE INDEX idx_document_casd_uuid  ON documentData (case_uuid);

DROP TABLE IF EXISTS audit_document;

CREATE TABLE IF NOT EXISTS audit_document
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

CREATE INDEX idx_audit_document_document_type ON audit_document (document_type);
CREATE INDEX idx_audit_document_status ON audit_document (status);
CREATE INDEX idx_audit_document_document_uuid ON audit_document (document_uuid);
CREATE INDEX idx_audit_document_case_uuid ON audit_document (case_uuid);
CREATE INDEX idx_audit_document_timestamp ON audit_document (timestamp);