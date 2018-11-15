DROP TABLE IF EXISTS case_data;

CREATE TABLE IF NOT EXISTS case_data
(
  id                    BIGSERIAL PRIMARY KEY,
  uuid                  UUID      NOT NULL,
  created               TIMESTAMP NOT NULL,
  type                  TEXT      NOT NULL,
  reference             TEXT      NOT NULL,
  data                  TEXT,
  primary_topic         UUID,
  primary_correspondent UUID,
  primary_response      UUID,
  deleted               BOOLEAN   NOT NULL DEFAULT FALSE,

  CONSTRAINT case_uuid_idempotent UNIQUE (uuid),
  CONSTRAINT case_ref_idempotent UNIQUE (reference)
);

DROP TABLE IF EXISTS stage;

SET DATABASE SQL SYNTAX PGS TRUE
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
