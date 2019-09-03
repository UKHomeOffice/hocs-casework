CREATE OR REPLACE VIEW active_stage_data AS
  SELECT c.reference AS case_reference, c.type AS case_type, c.data as data, s.*
  FROM stage s
         JOIN active_case c ON s.case_uuid = c.uuid
  WHERE s.team_uuid NOTNULL AND s.created = (SELECT MAX(ss.created) FROM stage ss WHERE ss.case_uuid = s.case_uuid);
