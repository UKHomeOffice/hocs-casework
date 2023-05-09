SET search_path TO casework;

ALTER TABLE case_data ADD COLUMN migrated_reference text DEFAULT NULL;
