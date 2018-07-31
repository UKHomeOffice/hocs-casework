DROP TABLE IF EXISTS screen_data;
DROP INDEX IF EXISTS idx_screen_type_stage_uuid;

DROP TABLE IF EXISTS active_stage_data;

CREATE TABLE IF NOT EXISTS active_stage_data
(
  id          BIGSERIAL PRIMARY KEY,
  screen_data JSONB NOT NULL,
  stage_type  TEXT  NOT NULL,
  stage_uuid  UUID  NOT NULL,

  CONSTRAINT fk_screen_id FOREIGN KEY (stage_uuid) REFERENCES stage_data (uuid)
);

CREATE INDEX idx_active_stage_data_type_stage_uuid
  ON active_stage_data (stage_type, stage_uuid);


DROP TABLE IF EXISTS active_stage;

CREATE TABLE IF NOT EXISTS active_stage
(
  id             BIGSERIAL PRIMARY KEY,
  case_type      TEXT NOT NULL,
  case_reference TEXT NOT NULL,
  case_uuid      UUID NOT NULL,
  stage_type     TEXT NOT NULL,
  stage_uuid     UUID NOT NULL,

  CONSTRAINT fk_active_stage_id FOREIGN KEY (stage_uuid) REFERENCES stage_data (uuid)
);

CREATE INDEX idx_active_stage_case_type_stage_type
  ON active_stage (case_type, stage_type);
