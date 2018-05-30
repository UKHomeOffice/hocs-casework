DROP TABLE IF EXISTS audit_case_data;

CREATE TABLE IF NOT EXISTS audit_case_data
(
  id              BIGSERIAL PRIMARY KEY,
  type            TEXT      NOT NULL,
  reference       TEXT      NOT NULL,
  uuid            UUID      NOT NULL,
  created         TIMESTAMP NOT NULL
);

CREATE INDEX idx_audit_case_data_type ON audit_case_data (type);
CREATE INDEX idx_audit_case_data_reference ON audit_case_data (reference);
CREATE INDEX idx_audit_case_data_uuid ON audit_case_data (uuid);
CREATE INDEX idx_audit_case_data_created ON audit_case_data (created);

DROP TABLE IF EXISTS audit_stage_data;

CREATE TABLE IF NOT EXISTS audit_stage_data
(
  id              BIGSERIAL PRIMARY KEY,
  uuid            UUID      NOT NULL,
  name            TEXT      NOT NULL,
  data            JSONB     NOT NULL,
  case_uuid       UUID      NOT NULL,
  schema_version  INT       NOT NULL,
  created         TIMESTAMP NOT NULL
);

CREATE INDEX idx_audit_stage_data_case_id ON audit_stage_data (case_uuid);
CREATE INDEX idx_audit_stage_data_uuid ON audit_stage_data (uuid);
CREATE INDEX idx_audit_stage_data_created ON audit_stage_data (created);


DROP TABLE IF EXISTS audit;

CREATE TABLE IF NOT EXISTS audit
(
  id              BIGSERIAL PRIMARY KEY,
  username        TEXT      NOT NULL,
  query_data      TEXT,
  case_id         Int,
  stage_id        Int,
  created         TIMESTAMP NOT NULL,
  event_action    TEXT      NOT NULL,

  CONSTRAINT fk_case_data_id FOREIGN KEY (case_id) REFERENCES audit_case_data(id),
  CONSTRAINT fk_stage_data_id FOREIGN KEY (stage_id) REFERENCES audit_stage_data(id)
);

CREATE INDEX idx_audit_username ON audit (username);
CREATE INDEX idx_audit_created ON audit (created);
CREATE INDEX idx_audit_action ON audit (event_action);