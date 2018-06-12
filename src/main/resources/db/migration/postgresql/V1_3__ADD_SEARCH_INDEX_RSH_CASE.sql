CREATE INDEX idx_stage_data_data_firstname ON stage_data (LOWER(CAST(data->'first-name' as text)));
CREATE INDEX idx_stage_data_data_lastname ON stage_data (LOWER(CAST(data->'last-name' as text)));
CREATE INDEX idx_stage_data_data_legacy_reference
  ON stage_data (LOWER(CAST(data -> 'legacy-reference' as text)));
CREATE INDEX idx_stage_data_data_date_birth ON stage_data USING gin ((data->'date-of-birth'));