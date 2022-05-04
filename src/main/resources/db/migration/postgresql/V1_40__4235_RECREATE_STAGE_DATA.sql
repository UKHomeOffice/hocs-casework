SET search_path TO casework;

CREATE OR REPLACE VIEW casework.stage_data(case_reference, case_type, data, case_created, id, uuid, created, type, deadline, transition_note_uuid, case_uuid, team_uuid, user_uuid, deadline_warning, correspondents, case_assigned_topic, somu) as
SELECT c.reference AS case_reference,
       c.type      AS case_type,
       c.data,
       c.created   AS case_created,
       s.id,
       s.uuid,
       s.created,
       s.type,
       s.deadline,
       s.transition_note_uuid,
       s.case_uuid,
       s.team_uuid,
       s.user_uuid,
       s.deadline_warning,
       cs.correspondents,
       t.text AS case_assigned_topic,
       s.somu,
       sc.uuid as secondary_case_uuid,
       sc.reference as secondary_case_reference,
       c.completed,
       sc.stage_uuid as secondary_stage_uuid
FROM casework.stage s
         LEFT JOIN casework.case_data c ON c.uuid = s.case_uuid
         LEFT JOIN LATERAL(
             SELECT json_build_object('correspondents', jsonb_agg(json_build_object('fullname', fullname, 'postcode', postcode, 'type', "type", 'is_primary', is_primary))) as correspondents
             FROM (
                 SELECT fullname,
                        postcode,
                        case_uuid,
                        type,
                        CASE WHEN uuid = c.primary_correspondent_uuid THEN 'true' ELSE 'false' END is_primary
                 FROM casework.correspondent
                 WHERE case_uuid = c.uuid
                   AND correspondent.deleted = false
                 ) co
         ) cs ON TRUE
         LEFT JOIN LATERAL (
             SELECT cd.reference,
                    cd.uuid,
                    s.uuid AS stage_uuid,
                    s.team_uuid,
                    cd.created
             FROM case_link cl
                 LEFT JOIN case_data cd ON cl.secondary_case_uuid = cd.uuid
                 LEFT JOIN stage s ON s.case_uuid = cd.uuid
             WHERE cl.primary_case_uuid = c.uuid
             ORDER BY s.team_uuid DESC NULLS LAST, s.created DESC limit 1
        ) sc ON TRUE
        LEFT JOIN casework.topic t ON c.primary_topic_uuid = t.uuid
WHERE NOT c.deleted;

DROP VIEW view_case_data;
