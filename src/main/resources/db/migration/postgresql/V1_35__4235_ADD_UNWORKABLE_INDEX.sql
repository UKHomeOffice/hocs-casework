SET search_path TO casework;

CREATE INDEX IF NOT EXISTS ixd_case_data_json ON case_data ((data->>'Unworkable'));
