ALTER TABLE correspondent_data
  ADD CONSTRAINT correspondent_uuid_idempotent UNIQUE (uuid);

ALTER TABLE case_correspondent
  ADD CONSTRAINT fk_case_id FOREIGN KEY (case_uuid) REFERENCES case_data (uuid);

ALTER TABLE case_correspondent
  ADD CONSTRAINT fk_correspondent_id FOREIGN KEY (correspondent_uuid) REFERENCES correspondent_data (uuid);
