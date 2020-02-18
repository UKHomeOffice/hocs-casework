DELETE FROM correspondent WHERE case_uuid = '14915b78-6977-42db-b343-0915a7f412a1' OR case_uuid = '24915b78-6977-42db-b343-0915a7f412a1';
DELETE FROM topic WHERE case_uuid = '14915b78-6977-42db-b343-0915a7f412a1' OR case_uuid = '24915b78-6977-42db-b343-0915a7f412a1';
DELETE FROM stage WHERE case_uuid = '14915b78-6977-42db-b343-0915a7f412a1' OR case_uuid = '24915b78-6977-42db-b343-0915a7f412a1';
DELETE FROM case_data WHERE type = 'TEST'; 