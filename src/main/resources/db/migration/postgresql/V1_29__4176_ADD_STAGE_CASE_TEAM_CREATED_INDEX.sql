SET search_path TO casework;

CREATE INDEX IF NOT EXISTS idx_stage_case_uuid_team_uuid_created ON stage (case_uuid, team_uuid DESC NULLS LAST, created DESC);
