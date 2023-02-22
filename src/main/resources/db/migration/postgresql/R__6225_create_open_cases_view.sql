SET search_path TO casework;

DROP VIEW IF EXISTS report_open_cases;

CREATE OR REPLACE VIEW report_open_cases AS
    SELECT cd.uuid                        AS case_uuid,
           cd.data ->> 'BusArea'          AS business_area,
           NOW()::DATE - cd.created::DATE AS age,
           case_deadline                  AS case_deadline,
           ast.type                       AS stage_type,
           cd.type                        AS user_group,
           case_deadline < NOW()          AS outside_service_standard
    FROM case_data cd
    LEFT JOIN active_stage ast ON cd.uuid = ast.case_uuid
    WHERE NOT completed;

