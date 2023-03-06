SET search_path TO casework;

DROP VIEW IF EXISTS report_open_cases;

CREATE OR REPLACE VIEW report_open_cases AS
    SELECT cd.uuid                        AS case_uuid,
           cd.reference                   AS case_reference,
           cd.data ->> 'BusArea'          AS business_area,
           NOW()::DATE - cd.created::DATE AS age,
           case_deadline                  AS case_deadline,
           ast.uuid                       AS stage_uuid,
           ast.type                       AS stage_type,
           ast.user_uuid                  AS assigned_user_uuid,
           ast.team_uuid                  AS assigned_team_uuid,
           cd.type                        AS user_group,
           case_deadline < NOW()          AS outside_service_standard
    FROM case_data cd
    LEFT JOIN active_stage ast ON cd.uuid = ast.case_uuid
    WHERE NOT completed;

