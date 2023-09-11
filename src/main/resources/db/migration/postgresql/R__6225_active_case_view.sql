SET search_path TO casework;

DROP VIEW IF EXISTS active_case;

CREATE VIEW active_case
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
