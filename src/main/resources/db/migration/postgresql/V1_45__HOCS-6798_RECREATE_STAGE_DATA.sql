-----RECREATING THIS VIEW TO REMOVE COMPLETED COLUMN WHICH WILL ALLOW NEXT VERSION OFSQL (V1_46__HOCS-6798_DROP_COMPLETED_column.sql)-----

SET search_path TO casework;

DROP VIEW IF EXISTS stage_data;

create view stage_data
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
