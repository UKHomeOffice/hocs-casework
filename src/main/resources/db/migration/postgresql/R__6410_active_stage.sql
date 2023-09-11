SET search_path TO casework;

--DROP VIEW IF EXISTS active_stage CASCADE;

create or replace view active_stage
            (id, uuid, created, type, deadline, transition_note_uuid, case_uuid, team_uuid, user_uuid,
             deadline_warning) as
SELECT s.id,
       s.uuid,
       s.created,
       s.type,
       s.deadline,
       s.transition_note_uuid,
       s.case_uuid,
       s.team_uuid,
       s.user_uuid,
       s.deadline_warning
FROM casework.stage s
WHERE s.team_uuid IS NOT NULL;

alter table active_stage
    owner to root;
