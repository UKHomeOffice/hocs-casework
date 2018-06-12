CREATE SEQUENCE case_ref
  START 0020001;

DROP TABLE IF EXISTS case_data;

CREATE TABLE IF NOT EXISTS case_data
(
  id        BIGSERIAL PRIMARY KEY,
  type      TEXT      NOT NULL,
  reference TEXT      NOT NULL,
  uuid      UUID      NOT NULL,
  timestamp TIMESTAMP NOT NULL,

  CONSTRAINT case_ref_idempotent UNIQUE (reference),
  CONSTRAINT case_uuid_idempotent UNIQUE (uuid)
);

CREATE INDEX idx_case_data_type ON case_data (type);
CREATE INDEX idx_case_data_reference ON case_data (reference);
CREATE INDEX idx_case_data_uuid ON case_data (uuid);
CREATE INDEX idx_case_data_timestamp
  ON case_data (timestamp);


DROP TABLE IF EXISTS stage_data;

CREATE TABLE IF NOT EXISTS stage_data
(
  id        BIGSERIAL PRIMARY KEY,
  uuid      UUID      NOT NULL,
  type      TEXT      NOT NULL,
  data      JSONB     NOT NULL,
  case_uuid UUID      NOT NULL,
  timestamp TIMESTAMP NOT NULL,

  CONSTRAINT stage_id_idempotent UNIQUE (uuid),
  CONSTRAINT fk_stage_id FOREIGN KEY (case_uuid) REFERENCES case_data(uuid)
);

CREATE INDEX idx_stage_data_uuid ON stage_data (uuid);
CREATE INDEX idx_stage_data_type
  ON stage_data (type);
CREATE INDEX idx_stage_data_case_id
  ON stage_data (case_uuid);
CREATE INDEX idx_stage_data_timestamp
  ON stage_data (timestamp);