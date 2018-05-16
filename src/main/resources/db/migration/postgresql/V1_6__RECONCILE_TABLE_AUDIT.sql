DROP TABLE IF EXISTS audit;

CREATE TABLE IF NOT EXISTS audit
(
  id              BIGSERIAL PRIMARY KEY,
  case_uuid       TEXT      NOT NULL,
  case_username   TEXT      NOT NULL,
  case_type       TEXT      NOT NULL,
  case_stage      TEXT      NOT NULL,
  event_uuid      TEXT      NOT NULL,
  event_timestamp TIMESTAMP NOT NULL,
  event_action    TEXT      NOT NULL,
  event_data      JSONB,

  CONSTRAINT audit_id_idempotent UNIQUE (event_uuid, event_timestamp)
);

CREATE INDEX idx_audit_username ON audit (case_uuid);
CREATE INDEX idx_audit_username ON audit (case_username);
CREATE INDEX idx_audit_timestamp ON audit (event_timestamp);