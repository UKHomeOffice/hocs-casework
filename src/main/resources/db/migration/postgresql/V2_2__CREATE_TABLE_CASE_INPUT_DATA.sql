DROP TABLE IF EXISTS case_input_data;

CREATE TABLE IF NOT EXISTS case_input_data
(
  id        BIGSERIAL PRIMARY KEY,
  data      JSONB,
  case_uuid UUID NOT NULL,

  CONSTRAINT cip_id_idempotent UNIQUE (case_uuid),
  CONSTRAINT fk_stage_id FOREIGN KEY (case_uuid) REFERENCES case_data (uuid)

);

CREATE INDEX idx_cip_case
  ON case_input_data (case_uuid);


ALTER TABLE stage_data
  DROP COLUMN data CASCADE;

ALTER TABLE active_stage
  add constraint active_stage_stage_uuid_idempotent UNIQUE (stage_uuid);