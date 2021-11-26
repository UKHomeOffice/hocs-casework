SET search_path TO casework;

CREATE INDEX IF NOT EXISTS idx_topic_case_uuid ON topic (case_uuid);
