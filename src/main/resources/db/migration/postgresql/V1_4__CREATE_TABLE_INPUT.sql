DROP TABLE IF EXISTS case_input_data cascade;

CREATE TABLE IF NOT EXISTS case_input_data
(
  id        BIGSERIAL PRIMARY KEY,
  data      JSONB,
  case_uuid UUID      NOT NULL,
  created   TIMESTAMP NOT NULL,
  updated   TIMESTAMP,

  CONSTRAINT cip_id_idempotent UNIQUE (case_uuid),
  CONSTRAINT fk_stage_id FOREIGN KEY (case_uuid) REFERENCES case_data (uuid)
);