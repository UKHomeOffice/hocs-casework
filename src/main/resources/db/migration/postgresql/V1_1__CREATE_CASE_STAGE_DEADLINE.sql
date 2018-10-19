DROP SEQUENCE IF EXISTS case_ref;

CREATE SEQUENCE case_ref
  START 0120001;

DROP TABLE IF EXISTS case_data;

CREATE TABLE IF NOT EXISTS case_data
(
  id        BIGSERIAL PRIMARY KEY,
  uuid      UUID      NOT NULL,
  created   TIMESTAMP NOT NULL,
  type      TEXT      NOT NULL,
  reference TEXT      NOT NULL,
  data      JSONB,
  deleted   BOOLEAN   NOT NULL DEFAULT FALSE,

  CONSTRAINT case_uuid_idempotent UNIQUE (uuid),
  CONSTRAINT case_ref_idempotent UNIQUE (reference)
);

DROP TABLE IF EXISTS stage;

CREATE TABLE IF NOT EXISTS stage
(
  id        BIGSERIAL PRIMARY KEY,
  uuid      UUID      NOT NULL,
  created   TIMESTAMP NOT NULL,
  type      TEXT      NOT NULL,
  completed BOOLEAN   NOT NULL DEFAULT FALSE,
  case_uuid UUID      NOT NULL,
  team_uuid UUID      NOT NULL,
  user_uuid UUID,

  CONSTRAINT stage_id_idempotent UNIQUE (uuid),
  CONSTRAINT fk_stage_id FOREIGN KEY (case_uuid) REFERENCES case_data (uuid)
);

DROP TABLE IF EXISTS stage_deadline cascade;

CREATE TABLE IF NOT EXISTS stage_deadline
(
  id         BIGSERIAL PRIMARY KEY,
  created    TIMESTAMP NOT NULL,
  stage_type TEXT      NOT NULL,
  date       DATE      NOT NULL,
  case_uuid  UUID      NOT NULL,

  CONSTRAINT fk_deadline_id FOREIGN KEY (case_uuid) REFERENCES case_data (uuid)
);