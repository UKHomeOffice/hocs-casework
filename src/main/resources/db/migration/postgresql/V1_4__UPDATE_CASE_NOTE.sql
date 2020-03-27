ALTER TABLE case_note
ADD COLUMN edited TIMESTAMP NULL;

ALTER TABLE case_note
ADD COLUMN editor TEXT NULL;

CREATE OR REPLACE VIEW active_case_note AS
  SELECT c.*
  FROM case_note c
         JOIN active_case ac ON c.case_uuid = ac.uuid
  WHERE NOT c.deleted;
