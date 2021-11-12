SET search_path TO casework;

DROP TRIGGER action_data_extensions_last_updated_timestamp ON action_data_appeals;
CREATE TRIGGER action_data_extensions_last_updated_timestamp
    BEFORE INSERT OR UPDATE ON action_data_extensions FOR EACH ROW EXECUTE PROCEDURE update_last_updated_timestamp_on_data_change();