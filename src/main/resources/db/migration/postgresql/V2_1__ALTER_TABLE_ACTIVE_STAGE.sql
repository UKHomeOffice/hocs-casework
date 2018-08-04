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
  assigned_team         TEXT NOT NULL,
  assigned_team_display TEXT NOT NULL,
  assigned_user         TEXT NOT NULL,
  assigned_user_display TEXT NOT NULL,

  CONSTRAINT fk_active_stage_id FOREIGN KEY (stage_uuid) REFERENCES stage_data (uuid)
);

CREATE INDEX idx_active_stage_assigned_team
  ON active_stage (assigned_team);

CREATE INDEX idx_active_stage_assigned_user
  ON active_stage (assigned_user);


ALTER TABLE stage_data
  add column case_reference TEXT NOT NULL;
