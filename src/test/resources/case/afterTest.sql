DELETE FROM correspondent WHERE case_uuid = '14915b78-6977-42db-b343-0915a7f412a1' OR case_uuid = '24915b78-6977-42db-b343-0915a7f412a1';
DELETE FROM topic WHERE case_uuid = '14915b78-6977-42db-b343-0915a7f412a1' OR case_uuid = '24915b78-6977-42db-b343-0915a7f412a1';
DELETE FROM stage WHERE case_uuid = '14915b78-6977-42db-b343-0915a7f412a1' OR case_uuid = '24915b78-6977-42db-b343-0915a7f412a1';
DELETE FROM stage WHERE case_uuid = 'b2e0b71c-d9be-4a0b-8e6c-38d06146f0e0';
DELETE FROM case_deadline_extension WHERE case_uuid = '14915b78-6977-42db-b343-0915a7f412a1' OR case_uuid = '24915b78-6977-42db-b343-0915a7f412a1';
DELETE FROM case_note WHERE case_uuid = '14915b78-6977-42db-b343-0915a7f412a1';
DELETE FROM case_deadline_extension_type WHERE type = 'TEST_EXTENSION';
DELETE FROM case_data WHERE type = 'TEST';
DELETE FROM bank_holiday;
