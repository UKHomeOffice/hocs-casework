-- update the active_case view to include linking to the next case
create or replace view casework.active_case(id, uuid, created, type, reference, data, primary_topic_uuid, primary_correspondent_uuid, case_deadline, date_received, deleted, completed, case_deadline_warning, secondary_case_uuid, secondary_case_reference, secondary_stage_uuid, primary_case_uuid, primary_case_reference, primary_stage_uuid) as
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
       case_data.completed,
       case_data.case_deadline_warning,
       next_case.uuid      AS secondary_case_uuid,
       next_case.reference AS secondary_case_reference,
       '00000000-0000-0000-0000-000000000000'::uuid as secondary_stage_uuid,
       prev_case.uuid      AS primary_case_uuid,
       prev_case.reference AS primary_case_reference,
       '00000000-0000-0000-0000-000000000000'::uuid as primary_stage_uuid
FROM casework.case_data
         LEFT JOIN casework.case_link pl ON case_data.uuid = pl.primary_case_uuid
         LEFT JOIN casework.case_data next_case ON pl.secondary_case_uuid = next_case.uuid
         LEFT JOIN casework.case_link sl on case_data.uuid = sl.secondary_case_uuid
         LEFT JOIN casework.case_data prev_case ON sl.primary_case_uuid = prev_case.uuid
WHERE NOT case_data.deleted;

-- create a view on case_data that includes the link in both directions
create or replace view casework.view_case_data(id, uuid, created, type, reference, data, primary_topic_uuid, primary_correspondent_uuid, case_deadline, date_received, deleted, completed, case_deadline_warning, secondary_case_uuid, secondary_case_reference, secondary_stage_uuid, primary_case_uuid, primary_case_reference, primary_stage_uuid) as
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
       case_data.completed,
       case_data.case_deadline_warning,
       next_case.uuid      AS secondary_case_uuid,
       next_case.reference AS secondary_case_reference,
       '00000000-0000-0000-0000-000000000000'::uuid as secondary_stage_uuid,
       prev_case.uuid            AS primary_case_uuid,
       prev_case.reference       AS primary_case_reference,
       '00000000-0000-0000-0000-000000000000'::uuid as primary_stage_uuid
FROM casework.case_data
         LEFT JOIN casework.case_link pl ON case_data.uuid = pl.primary_case_uuid
         LEFT JOIN casework.case_data next_case ON pl.secondary_case_uuid = next_case.uuid
         LEFT JOIN casework.case_link sl on case_data.uuid = sl.secondary_case_uuid
         LEFT JOIN casework.case_data prev_case ON sl.primary_case_uuid = prev_case.uuid;
