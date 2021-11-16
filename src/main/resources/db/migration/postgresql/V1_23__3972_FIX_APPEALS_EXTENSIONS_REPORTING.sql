SET search_path TO casework;

DROP TRIGGER action_data_extensions_last_updated_timestamp ON action_data_appeals;
DROP TRIGGER action_data_appeals_last_updated_timestamp ON action_data_appeals;
DROP TRIGGER action_data_external_interest_last_updated_timestamp ON action_data_appeals;

ALTER TABLE action_data_extensions
    ALTER COLUMN created_timestamp DROP DEFAULT,
    ALTER COLUMN created_timestamp SET NOT NULL,
    ALTER COLUMN last_updated_timestamp SET NOT NULL;

ALTER TABLE action_data_appeals
    ALTER COLUMN created_timestamp DROP DEFAULT,
    ALTER COLUMN created_timestamp SET NOT NULL,
    ALTER COLUMN last_updated_timestamp SET NOT NULL;

ALTER TABLE action_data_external_interest
    ALTER COLUMN created_timestamp DROP DEFAULT,
    ALTER COLUMN created_timestamp SET NOT NULL,
    ALTER COLUMN last_updated_timestamp SET NOT NULL;

