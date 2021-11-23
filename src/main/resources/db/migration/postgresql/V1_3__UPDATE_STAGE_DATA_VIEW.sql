DROP VIEW IF EXISTS stage_data;

CREATE OR REPLACE VIEW stage_data AS
  SELECT c.reference AS case_reference, c.type AS case_type, c.data as data, c.created AS case_created, s.*
  FROM stage s
         JOIN active_case c ON s.case_uuid = c.uuid;

DROP VIEW IF EXISTS active_stage_data;

CREATE OR REPLACE VIEW active_stage_data AS
  SELECT c.reference AS case_reference, c.type AS case_type, c.data as data, c.created AS case_created, s.*
  FROM stage s
         JOIN active_case c ON s.case_uuid = c.uuid
  WHERE s.team_uuid NOTNULL;
