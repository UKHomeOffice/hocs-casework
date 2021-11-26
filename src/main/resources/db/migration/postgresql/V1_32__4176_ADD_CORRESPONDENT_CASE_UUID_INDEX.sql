SET search_path TO casework;

CREATE INDEX IF NOT EXISTS idx_correspondent_case_uuid ON correspondent (case_uuid);
