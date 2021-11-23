
ALTER TABLE case_data
    ADD COLUMN case_deadline_warning DATE NULL;

COMMENT ON COLUMN case_data.case_deadline_warning is 'Date from which to display warning that case is approaching deadline.';

-- need to re-create this view after base table changes above
CREATE OR REPLACE VIEW active_case AS
  SELECT *
  FROM case_data
  WHERE NOT deleted;

ALTER TABLE stage
    ADD COLUMN deadline_warning DATE NULL;

COMMENT ON COLUMN stage.deadline_warning is 'Date from which to display warning that stage for case is approaching deadline.';

-- need to re-create these views after base table changes above
CREATE OR REPLACE VIEW active_stage AS
  SELECT s.*
  FROM stage s
  WHERE s.team_uuid notnull;

CREATE OR REPLACE VIEW stage_data AS
  SELECT c.reference AS case_reference, c.type AS case_type, c.data as data, c.created AS case_created, s.*
  FROM stage s
         JOIN active_case c ON s.case_uuid = c.uuid;

CREATE OR REPLACE VIEW active_stage_data AS
  SELECT c.reference AS case_reference, c.type AS case_type, c.data as data, c.created AS case_created, s.*
  FROM stage s
         JOIN active_case c ON s.case_uuid = c.uuid
  WHERE s.team_uuid NOTNULL;
