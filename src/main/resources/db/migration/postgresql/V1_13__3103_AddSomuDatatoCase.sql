ALTER TABLE stage
    ADD COLUMN somu JSONB;

CREATE OR REPLACE VIEW active_stage_data AS
SELECT c.reference AS case_reference, c.type AS case_type, c.data as data, c.created AS case_created, s.id, s.uuid, s.created, s.type, s.deadline, s.transition_note_uuid, s.case_uuid, s.team_uuid, s.user_uuid, s.deadline_warning, correspondents, t.text AS case_assigned_topic, s.somu
FROM stage s
         JOIN active_case c ON s.case_uuid = c.uuid
         LEFT JOIN casework.correspondents_json_by_case cs ON s.case_uuid = cs.case_uuid
         LEFT JOIN casework.topic t ON c.primary_topic_uuid = t.uuid
WHERE s.team_uuid NOTNULL;

CREATE OR REPLACE VIEW casework.stage_data AS
SELECT c.reference AS case_reference, c.type AS case_type, c.data as data, c.created AS case_created, s.id, s.uuid, s.created, s.type, s.deadline, s.transition_note_uuid, s.case_uuid, s.team_uuid, s.user_uuid, s.deadline_warning, correspondents, t.text AS case_assigned_topic, s.somu
FROM casework.stage s
         JOIN casework.active_case c ON s.case_uuid = c.uuid
         LEFT JOIN casework.correspondents_json_by_case cs ON s.case_uuid = cs.case_uuid
         LEFT JOIN casework.topic t ON c.primary_topic_uuid = t.uuid;
