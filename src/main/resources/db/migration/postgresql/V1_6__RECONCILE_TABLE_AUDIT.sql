DROP TABLE IF EXISTS audit;

CREATE TABLE IF NOT EXISTS audit
(
  id              BIGSERIAL PRIMARY KEY,
  username        TEXT      NOT NULL,
  case_uuid       UUID      ,
  case_stage      TEXT      ,
  timestamp       TIMESTAMP NOT NULL,
  event_action    TEXT      NOT NULL,
  event_data      JSONB
);

CREATE INDEX idx_audit_username ON audit (username);
CREATE INDEX idx_audit_uuid ON audit (case_uuid);
CREATE INDEX idx_audit_timestamp ON audit (timestamp);