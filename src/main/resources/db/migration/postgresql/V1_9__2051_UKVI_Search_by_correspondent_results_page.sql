CREATE OR REPLACE VIEW active_stage AS
  SELECT s.*
  FROM stage s
  WHERE s.team_uuid notnull;

CREATE OR REPLACE VIEW casework.correspondents_json_by_case AS
    SELECT case_uuid, json_build_object('correspondents', jsonb_agg(json_build_object('fullname', fullname, 'type', "type"))) as correspondents
    FROM (
      SELECT fullname, case_uuid, "type" FROM casework.correspondent WHERE casework.correspondent.deleted = false
    ) correspondents_by_case
    GROUP BY case_uuid;

CREATE OR REPLACE VIEW casework.stage_data AS
  SELECT c.reference AS case_reference, c.type AS case_type, c.data as data, c.created AS case_created, s.*, correspondents
  FROM casework.stage s
    JOIN casework.active_case c ON s.case_uuid = c.uuid
    LEFT JOIN casework.correspondents_json_by_case cs ON s.case_uuid = cs.case_uuid;

CREATE OR REPLACE VIEW active_stage_data AS
  SELECT c.reference AS case_reference, c.type AS case_type, c.data as data, c.created AS case_created, s.*, correspondents
  FROM stage s
         JOIN active_case c ON s.case_uuid = c.uuid
         LEFT JOIN casework.correspondents_json_by_case cs ON s.case_uuid = cs.case_uuid
  WHERE s.team_uuid NOTNULL;
