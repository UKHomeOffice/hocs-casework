create table casework.case_link
(
    primary_case_uuid uuid not null
        constraint case_link_primary___fk
            references casework.case_data (uuid),
    secondary_case_uuid uuid not null
        constraint case_link_secondary__fk
            references casework.case_data (uuid),
    constraint case_link_pk
        primary key (primary_case_uuid, secondary_case_uuid)
);

comment on table casework.case_link is 'Stores a link between a primary and secondary case';

-- update the active_case view to include linking to the next case
create or replace view casework.active_case(id, uuid, created, type, reference, data, primary_topic_uuid, primary_correspondent_uuid, case_deadline, date_received, deleted, completed, case_deadline_warning, secondary_case_uuid, secondary_case_reference, primary_case_uuid, primary_case_reference) as
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
       prev_case.uuid            AS primary_case_uuid,
       prev_case.reference       AS primary_case_reference
FROM casework.case_data
         LEFT JOIN casework.case_link pl ON case_data.uuid = pl.primary_case_uuid
         LEFT JOIN casework.case_data next_case ON pl.secondary_case_uuid = next_case.uuid
         LEFT JOIN casework.case_link sl on case_data.uuid = sl.secondary_case_uuid
         LEFT JOIN casework.case_data prev_case ON sl.primary_case_uuid = prev_case.uuid
WHERE NOT case_data.deleted;

-- create a view on case_data that includes the link in both directions
create or replace view casework.view_case_data(id, uuid, created, type, reference, data, primary_topic_uuid, primary_correspondent_uuid, case_deadline, date_received, deleted, completed, case_deadline_warning, secondary_case_uuid, secondary_case_reference, primary_case_uuid, primary_case_reference) as
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
       prev_case.uuid            AS primary_case_uuid,
       prev_case.reference       AS primary_case_reference
FROM casework.case_data
         LEFT JOIN casework.case_link pl ON case_data.uuid = pl.primary_case_uuid
         LEFT JOIN casework.case_data next_case ON pl.secondary_case_uuid = next_case.uuid
         LEFT JOIN casework.case_link sl on case_data.uuid = sl.secondary_case_uuid
         LEFT JOIN casework.case_data prev_case ON sl.primary_case_uuid = prev_case.uuid;

-- Add the secondary case details to the stage_data
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
       c.completed
FROM casework.stage s
         JOIN casework.active_case c ON s.case_uuid = c.uuid
         LEFT JOIN casework.correspondents_json_by_case cs ON s.case_uuid = cs.case_uuid
         LEFT JOIN casework.topic t ON c.primary_topic_uuid = t.uuid;

create or replace view casework.active_stage_data(case_reference, case_type, data, case_created, id, uuid, created, type, deadline, transition_note_uuid, case_uuid, team_uuid, user_uuid, deadline_warning, correspondents, case_assigned_topic, somu) as
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
       t.text      AS case_assigned_topic,
       s.somu,
       c.secondary_case_uuid,
       c.secondary_case_reference,
       c.completed
FROM casework.stage s
         JOIN casework.active_case c ON s.case_uuid = c.uuid
         LEFT JOIN casework.correspondents_json_by_case cs ON s.case_uuid = cs.case_uuid
         LEFT JOIN casework.topic t ON c.primary_topic_uuid = t.uuid
WHERE s.team_uuid IS NOT NULL;


