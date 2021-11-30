SET search_path TO casework;

create or replace view casework.stage_data(case_reference, case_type, data, case_created, id, uuid, created, type, deadline, transition_note_uuid, case_uuid, team_uuid, user_uuid, deadline_warning, correspondents, case_assigned_topic, somu) as
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
       c.secondary_case_uuid,
       c.secondary_case_reference,
       c.completed,
       c.secondary_stage_uuid
FROM casework.stage s
         JOIN casework.view_case_data c ON s.case_uuid = c.uuid
         LEFT JOIN LATERAL(
    SELECT json_build_object('correspondents', jsonb_agg(json_build_object('fullname', fullname, 'postcode', postcode, 'type', "type", 'is_primary', is_primary))) as correspondents
    FROM (SELECT fullname,postcode, case_uuid, type, case when uuid = c.primary_correspondent_uuid then 'true' else 'false' end is_primary
          FROM casework.correspondent WHERE case_uuid = c.uuid AND correspondent.deleted = false) co) cs ON TRUE
         LEFT JOIN casework.topic t ON c.primary_topic_uuid = t.uuid
WHERE NOT c.deleted;

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
       secondary_case.uuid      AS secondary_case_uuid,
       secondary_case.reference AS secondary_case_reference,
       secondary_case.stage_uuid as secondary_stage_uuid,
       primary_case.uuid      AS primary_case_uuid,
       primary_case.reference AS primary_case_reference,
       primary_case.stage_uuid as primary_stage_uuid
FROM casework.case_data
         LEFT JOIN LATERAL (SELECT c.reference, c.uuid, s.uuid AS stage_uuid, s.team_uuid, c.created
                            FROM case_link cl
                            LEFT JOIN case_data c on cl.secondary_case_uuid = c.uuid
                            LEFT JOIN stage s on s.case_uuid = c.uuid
                            WHERE cl.primary_case_uuid = case_data.uuid
                            ORDER BY s.team_uuid DESC NULLS LAST, s.created DESC limit 1) secondary_case ON TRUE
         LEFT JOIN LATERAL (SELECT c.reference, c.uuid, s.uuid AS stage_uuid, s.team_uuid, c.created
                            FROM case_link cl
                            LEFT JOIN case_data c ON cl.primary_case_uuid = c.uuid
                            LEFT JOIN stage s on s.case_uuid = c.uuid
                            WHERE cl.secondary_case_uuid = case_data.uuid
                            ORDER BY s.team_uuid DESC NULLS LAST, s.created DESC limit 1) primary_case ON TRUE;

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
       secondary_case.uuid      AS secondary_case_uuid,
       secondary_case.reference AS secondary_case_reference,
       secondary_case.stage_uuid as secondary_stage_uuid,
       primary_case.uuid      AS primary_case_uuid,
       primary_case.reference AS primary_case_reference,
       primary_case.stage_uuid as primary_stage_uuid
FROM casework.case_data
         LEFT JOIN LATERAL (SELECT c.reference, c.uuid, s.uuid AS stage_uuid, s.team_uuid, c.created
                            FROM case_link cl
                            LEFT JOIN case_data c on cl.secondary_case_uuid = c.uuid
                            LEFT JOIN stage s on s.case_uuid = c.uuid
                            WHERE cl.primary_case_uuid = case_data.uuid
                            ORDER BY s.team_uuid DESC NULLS LAST, s.created DESC limit 1) secondary_case ON TRUE
         LEFT JOIN LATERAL (SELECT c.reference, c.uuid, s.uuid AS stage_uuid, s.team_uuid, c.created
                            FROM case_link cl
                            LEFT JOIN case_data c ON cl.primary_case_uuid = c.uuid
                            LEFT JOIN stage s on s.case_uuid = c.uuid
                            WHERE cl.secondary_case_uuid = case_data.uuid
                            ORDER BY s.team_uuid DESC NULLS LAST, s.created DESC limit 1) primary_case ON TRUE;
WHERE NOT c.deleted;

create index if not exists idx_case_type ON case_data USING btree (type);
create index if not exists ixd_case_data_unworkable on case_data ((data->>'Unworkable'));
create index if not exists idx_case_data_uuid_reference on case_data (uuid, reference);

create index if not exists idx_stage_user_uuid_team_uuid on stage (team_uuid, user_uuid) where team_UUID IS NOT NULL;
create index if not exists idx_stage_deadline on stage (team_uuid, deadline);

drop view active_stage_data;
