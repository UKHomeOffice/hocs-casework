SET search_path TO casework;

DROP VIEW IF EXISTS report_work_in_progress;

CREATE OR REPLACE VIEW report_work_in_progress AS
    SELECT cd.uuid                                 AS case_uuid,
           cd.reference                            AS case_reference,
           cd.data ->> 'CompType'                  AS comp_type,
           cd.created::date                        AS date_created,
           cd.data ->> 'ReceivedDate'              AS date_received,
           cd.case_deadline                        AS case_deadline,
           cd.data ->> 'OwningCSU'                 AS owning_csu,
           cd.data ->> 'Directorate'               AS directorate,
           cd.data ->> 'BusAreaBasedOnDirectorate' AS business_area_based_on_directorate,
           cd.data ->> 'EnqReason'                 AS enquiry_reason,
           c.fullname                              AS primary_correspondent_name,
           cd.data ->> 'CaseSummary'               AS case_summary,
           cd.data ->> 'Severity'                  AS severity,
           ast.user_uuid                           AS assigned_user_uuid,
           ast.team_uuid                           AS assigned_team_uuid,
           ast.uuid                                AS stage_uuid,
           ast.type                                AS stage_type,
           cd.type                                 AS case_type,
           CASE
               WHEN cd.case_deadline < now()::date THEN 'Outside service standard'
               WHEN extract(WEEK FROM cd.case_deadline) = extract(WEEK FROM now()) THEN 'Due this week'
               WHEN extract(WEEK FROM cd.case_deadline) = extract(WEEK FROM now() + INTERVAL '1 WEEK') THEN 'Due next week'
               WHEN extract(WEEK FROM cd.case_deadline) = extract(WEEK FROM now() + INTERVAL '2 WEEK') THEN 'Due week 3'
               WHEN extract(WEEK FROM cd.case_deadline) = extract(WEEK FROM now() + INTERVAL '3 WEEK') THEN 'Due week 4'
               WHEN extract(WEEK FROM cd.case_deadline) = extract(WEEK FROM now() + INTERVAL '4 WEEK') THEN 'Due week 5'
           END                                     AS due_week
    FROM case_data cd
    LEFT JOIN active_stage ast ON cd.uuid = ast.case_uuid
    LEFT JOIN correspondent c ON cd.uuid = c.case_uuid
    WHERE NOT completed;

