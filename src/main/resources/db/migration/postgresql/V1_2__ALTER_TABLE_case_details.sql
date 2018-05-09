CREATE SEQUENCE ref_seq START 1000001;


ALTER TABLE case_details ALTER COLUMN case_data type JSONB USING case_data::JSONB;
ALTER TABLE rsh_case_details ALTER COLUMN case_data type JSONB USING case_data::JSONB;