-----RECREATING THIS VIEW TO REMOVE COMPLETED COLUMN WHICH WILL ALLOW NEXT VERSION OFSQL (V1_46__HOCS-6798_DROP_COMPLETED_column.sql)-----

SET search_path TO casework;

create or replace view stage_data
            (case_reference, case_type, data, case_created, id, uuid, created, type, deadline, transition_note_uuid,
             case_uuid, team_uuid, user_uuid, deadline_warning, correspondents, case_assigned_topic, somu,
             secondary_case_uuid, secondary_case_reference, date_completed, secondary_stage_uuid)
as
SELECT c.reference   AS case_reference,
       c.type        AS case_type,
       c.data,
       c.created     AS case_created,
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
       t.text        AS case_assigned_topic,
       s.somu,
       sc.uuid       AS secondary_case_uuid,
       sc.reference  AS secondary_case_reference,
       c.date_completed,
       sc.stage_uuid AS secondary_stage_uuid
FROM casework.stage s
         LEFT JOIN casework.case_data c ON c.uuid = s.case_uuid
         LEFT JOIN LATERAL ( SELECT json_build_object('correspondents',
                                                      json_agg(json_build_object('fullname', co.fullname, 'postcode',
                                                                                 co.postcode, 'type', co.type,
                                                                                 'is_primary',
                                                                                 co.is_primary))) AS correspondents
                             FROM (SELECT correspondent.fullname,
                                          correspondent.postcode,
                                          correspondent.case_uuid,
                                          correspondent.type,
                                          CASE
                                              WHEN correspondent.uuid = c.primary_correspondent_uuid THEN 'true'::text
                                              ELSE 'false'::text
                                              END AS is_primary
                                   FROM casework.correspondent
                                   WHERE correspondent.case_uuid = c.uuid
                                     AND correspondent.deleted = false) co) cs ON true
         LEFT JOIN LATERAL ( SELECT cd.reference,
                                    cd.uuid,
                                    s_1.uuid AS stage_uuid,
                                    s_1.team_uuid,
                                    cd.created
                             FROM casework.case_link cl
                                      LEFT JOIN casework.case_data cd ON cl.secondary_case_uuid = cd.uuid
                                      LEFT JOIN casework.stage s_1 ON s_1.case_uuid = cd.uuid
                             WHERE cl.primary_case_uuid = c.uuid
                             ORDER BY s_1.team_uuid DESC NULLS LAST, s_1.created DESC
                             LIMIT 1) sc ON true
         LEFT JOIN casework.topic t ON c.primary_topic_uuid = t.uuid
WHERE NOT c.deleted;

create or replace view active_case
            (id, uuid, created, type, reference, data, primary_topic_uuid, primary_correspondent_uuid, case_deadline,
             date_received, deleted, date_completed, case_deadline_warning, secondary_case_uuid, secondary_case_reference,
             secondary_stage_uuid, primary_case_uuid, primary_case_reference, primary_stage_uuid, migrated_reference)
AS
SELECT case_data.id,
       case_data.uuid,
       case_data.created,
       case_data.type,
       case_data.reference,
       case_data.data,
       case_data.primary_topic_uuid,
       case_data.primary_correspondent_uuid,
       case_data.case_deadline,
       case_data.date_received,
       case_data.deleted,
       case_data.date_completed,
       case_data.case_deadline_warning,
       secondary_case.uuid       AS secondary_case_uuid,
       secondary_case.reference  AS secondary_case_reference,
       secondary_case.stage_uuid AS secondary_stage_uuid,
       primary_case.uuid         AS primary_case_uuid,
       primary_case.reference    AS primary_case_reference,
       primary_case.stage_uuid   AS primary_stage_uuid,
       case_data.migrated_reference
FROM casework.case_data
         LEFT JOIN LATERAL ( SELECT c.reference,
                                    c.uuid,
                                    s.uuid AS stage_uuid,
                                    s.team_uuid,
                                    c.created
                             FROM casework.case_link cl
                                      LEFT JOIN casework.case_data c ON cl.secondary_case_uuid = c.uuid
                                      LEFT JOIN casework.stage s ON s.case_uuid = c.uuid
                             WHERE cl.primary_case_uuid = case_data.uuid
                             ORDER BY s.team_uuid DESC NULLS LAST, s.created DESC
                             LIMIT 1) secondary_case ON TRUE
         LEFT JOIN LATERAL ( SELECT c.reference,
                                    c.uuid,
                                    s.uuid AS stage_uuid,
                                    s.team_uuid,
                                    c.created
                             FROM casework.case_link cl
                                      LEFT JOIN casework.case_data c ON cl.primary_case_uuid = c.uuid
                                      LEFT JOIN casework.stage s ON s.case_uuid = c.uuid
                             WHERE cl.secondary_case_uuid = case_data.uuid
                             ORDER BY s.team_uuid DESC NULLS LAST, s.created DESC
                             LIMIT 1) primary_case ON TRUE
WHERE NOT case_data.deleted;
