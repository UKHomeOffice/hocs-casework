SET search_path TO casework;

DELETE FROM case_note WHERE case_uuid='14915b78-6977-42db-b343-0915a7f412a1';
DELETE FROM action_data_extensions WHERE case_data_uuid = '14915b78-6977-42db-b343-0915a7f412a1';
DELETE FROM action_data_appeals WHERE case_data_uuid = '14915b78-6977-42db-b343-0915a7f412a1';
DELETE FROM action_data_external_interest WHERE case_data_uuid = '14915b78-6977-42db-b343-0915a7f412a1';
DELETE FROM correspondent WHERE case_uuid = '14915b78-6977-42db-b343-0915a7f412a1';
DELETE FROM stage WHERE case_uuid = '14915b78-6977-42db-b343-0915a7f412a1';
DELETE FROM case_data WHERE uuid = '14915b78-6977-42db-b343-0915a7f412a1';
