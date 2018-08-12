DROP TABLE IF EXISTS active_stage_data cascade;

DROP TABLE IF EXISTS active_stage;

CREATE TABLE IF NOT EXISTS active_stage
(
  id                    BIGSERIAL PRIMARY KEY,
  case_uuid             UUID NOT NULL,
  stage_uuid            UUID NOT NULL,
  case_reference        TEXT NOT NULL,
  case_type             TEXT NOT NULL,
  stage_type            TEXT NOT NULL,
  team_uuid             UUID,
  assigned_team_display TEXT,
  user_uuid             UUID,
  assigned_user_display TEXT,

  CONSTRAINT fk_active_stage_id FOREIGN KEY (stage_uuid) REFERENCES stage_data (uuid)
);

CREATE INDEX idx_active_stage_assigned_team
  ON active_stage (team_uuid);

CREATE INDEX idx_active_stage_assigned_user
  ON active_stage (user_uuid);

ALTER TABLE stage_data
  add column team_uuid UUID;
ALTER TABLE stage_data
  add column user_uuid UUID;