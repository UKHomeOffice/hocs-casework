SET search_path TO casework;

ALTER TABLE correspondent
    ADD COLUMN organisation TEXT;

CREATE OR REPLACE VIEW active_correspondent AS
  SELECT c.*
  FROM correspondent c
         JOIN active_case ac ON c.case_uuid = ac.uuid
  WHERE NOT c.deleted;

CREATE OR REPLACE VIEW primary_correspondent AS
  SELECT c.*
  FROM correspondent c
         JOIN active_case ac ON c.uuid = ac.primary_correspondent_uuid
  WHERE NOT c.deleted;