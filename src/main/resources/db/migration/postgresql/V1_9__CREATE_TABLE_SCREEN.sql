DROP TABLE IF EXISTS screen_data;

CREATE TABLE IF NOT EXISTS screen_data
(
  id         BIGSERIAL PRIMARY KEY,
  type       TEXT  NOT NULL,
  data       JSONB NOT NULL,
  stage_uuid UUID  NOT NULL,
  CONSTRAINT fk_screen_id FOREIGN KEY (stage_uuid) REFERENCES stage_data (uuid)
);

CREATE INDEX idx_screen_type_stage_uuid
  ON screen_data (type, stage_uuid);
