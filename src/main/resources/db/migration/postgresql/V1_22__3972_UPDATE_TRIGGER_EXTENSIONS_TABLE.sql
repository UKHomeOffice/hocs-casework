SET search_path TO casework;

DROP TRIGGER action_data_extensions_last_updated_timestamp ON action_data_appeals;
DROP TRIGGER action_data_appeals_last_updated_timestamp ON action_data_appeals;

ALTER TABLE action_data_extensions
    ALTER COLUMN created_timestamp DROP DEFAULT,
    ALTER COLUMN created_timestamp SET NOT NULL,
    ALTER COLUMN last_updated_timestamp SET NOT NULL;

ALTER TABLE action_data_appeals
    ALTER COLUMN created_timestamp DROP DEFAULT,
    ALTER COLUMN created_timestamp SET NOT NULL,
    ALTER COLUMN last_updated_timestamp SET NOT NULL;



-- CREATE TRIGGER action_data_extensions_last_updated_timestamp
--     BEFORE INSERT OR UPDATE ON action_data_extensions FOR EACH ROW EXECUTE PROCEDURE update_last_updated_timestamp_on_data_change();
--
-- CREATE OR REPLACE FUNCTION created_timestamp_on_data_insert()
--     RETURNS TRIGGER AS $$
-- BEGIN
--     IF (tg_op = 'INSERT') THEN
--         NEW.created_timestamp = now();
--         RETURN NEW;
--     END IF;
-- END;
-- $$ language 'plpgsql';
--
-- CREATE TRIGGER action_data_extensions_created_timestamp
--     BEFORE INSERT ON action_data_extensions FOR EACH ROW EXECUTE PROCEDURE created_timestamp_on_data_insert();
--
-- CREATE TRIGGER action_data_appeals_created_timestamp
--     BEFORE INSERT ON action_data_appeals FOR EACH ROW EXECUTE PROCEDURE created_timestamp_on_data_insert();