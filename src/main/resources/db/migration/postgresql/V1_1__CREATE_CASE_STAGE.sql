DROP SEQUENCE IF EXISTS case_ref;

CREATE SEQUENCE case_ref
  START 0120001;

DROP TABLE IF EXISTS case_data;

CREATE TABLE IF NOT EXISTS case_data
(
  id                    BIGSERIAL PRIMARY KEY,
  uuid                  UUID      NOT NULL,
  created               TIMESTAMP NOT NULL,
  type                  TEXT      NOT NULL,
  reference             TEXT      NOT NULL,
  data                  JSONB,
  primary_topic         UUID,
  primary_correspondent UUID,
  primary_response      UUID,
  deleted               BOOLEAN   NOT NULL DEFAULT FALSE,

  CONSTRAINT case_uuid_idempotent UNIQUE (uuid),
  CONSTRAINT case_ref_idempotent UNIQUE (reference)
);

CREATE OR REPLACE VIEW active_case AS
  SELECT *
  FROM case_data
  WHERE deleted = FALSE;

DROP TABLE IF EXISTS stage;

CREATE TABLE IF NOT EXISTS stage
(
  id        BIGSERIAL PRIMARY KEY,
  uuid      UUID      NOT NULL,
  created   TIMESTAMP NOT NULL,
  type      TEXT      NOT NULL,
  deadline  DATE,
  status    TEXT      NOT NULL,
  case_uuid UUID      NOT NULL,
  team_uuid UUID,
  user_uuid UUID,

  CONSTRAINT stage_id_idempotent UNIQUE (uuid),
  CONSTRAINT fk_stage_id FOREIGN KEY (case_uuid) REFERENCES case_data (uuid)
);

CREATE OR REPLACE VIEW active_stage AS
  SELECT *
  FROM stage
  WHERE status != 'COMPLETE';

CREATE OR REPLACE VIEW stage_data AS
  SELECT c.reference AS case_reference, c.type AS case_type, c.data as data, s.*
  FROM active_stage s
         JOIN active_case c ON s.case_uuid = c.uuid;