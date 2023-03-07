DELETE FROM casework.correspondent c USING casework.case_data cd WHERE c.case_uuid = cd.uuid AND cd.type = 'TEST';
DELETE FROM casework.case_note cn USING casework.case_data cd WHERE cn.case_uuid = cd.uuid AND cd.type = 'TEST';
DELETE FROM casework.case_link cl USING casework.case_data cd WHERE (cl.primary_case_uuid = cd.uuid OR cl.secondary_case_uuid = cd.uuid) AND cd.type = 'TEST';
DELETE FROM casework.stage s USING casework.case_data cd WHERE s.case_uuid = cd.uuid AND cd.type = 'TEST';
DELETE FROM casework.topic t  USING casework.case_data cd WHERE t.case_uuid = cd.uuid AND cd.type = 'TEST';
DELETE FROM casework.case_data WHERE type = 'TEST';
