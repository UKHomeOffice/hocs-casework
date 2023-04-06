SET search_path TO casework;

ALTER TABLE case_data ADD COLUMN date_completed timestamp DEFAULT NULL;
