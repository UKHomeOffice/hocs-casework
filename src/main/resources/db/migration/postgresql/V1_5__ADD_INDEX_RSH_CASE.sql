CREATE INDEX idx_rsh_case_data_firstname ON rsh_case (LOWER(CAST(data->'first-name' as text)));
CREATE INDEX idx_rsh_case_data_lastname ON rsh_case (LOWER(CAST(data->'last-name' as text)));
CREATE INDEX idx_rsh_case_data_date_birth ON rsh_case USING gin ((data->'date-of-birth'));
CREATE INDEX idx_rsh_case_reference ON rsh_case (reference);
CREATE INDEX idx_rsh_case_uuid ON rsh_case (uuid);

CREATE INDEX idx_audit_username ON audit (username);
CREATE INDEX idx_audit_timestamp ON audit (timestamp);
