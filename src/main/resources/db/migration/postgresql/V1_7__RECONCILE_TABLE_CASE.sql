DROP TABLE IF EXISTS case_details;

CREATE TABLE IF NOT EXISTS case_details
(
  id              BIGSERIAL PRIMARY KEY,
  type            TEXT      NOT NULL,
  reference       TEXT      NOT NULL,
  uuid            UUID      NOT NULL,
  created         TIMESTAMP NOT NULL,

  CONSTRAINT case_ref_idempotent UNIQUE (reference),
  CONSTRAINT case_uuid_idempotent UNIQUE (uuid)
);

CREATE INDEX idx_case_details_type ON case_details (type);
CREATE INDEX idx_case_details_reference ON case_details (reference);
CREATE INDEX idx_case_details_uuid ON case_details (uuid);
CREATE INDEX idx_case_details_created ON case_details (created);


DROP TABLE IF EXISTS stage_details;

CREATE TABLE IF NOT EXISTS stage_details
(
  id              BIGSERIAL PRIMARY KEY,
  uuid            UUID      NOT NULL,
  name            TEXT      NOT NULL,
  data            JSONB     NOT NULL,
  case_uuid       UUID      NOT NULL,
  schema_version  INT       NOT NULL,
  created         TIMESTAMP NOT NULL,
  updated         TIMESTAMP NOT NULL,

  CONSTRAINT stage_id_idempotent UNIQUE (uuid),
  CONSTRAINT fk_stage_id FOREIGN KEY (case_uuid) REFERENCES case_details(uuid)
);

CREATE INDEX idx_stage_details_case_id ON stage_details (case_uuid);
CREATE INDEX idx_stage_details_uuid ON stage_details (uuid);
CREATE INDEX idx_stage_details_created ON stage_details (created);
CREATE INDEX idx_stage_details_updated ON stage_details (updated);