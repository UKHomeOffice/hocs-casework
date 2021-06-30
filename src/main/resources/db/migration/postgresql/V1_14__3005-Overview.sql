CREATE OR REPLACE VIEW casework.case_overview
AS SELECT c.id,
          c.uuid::text AS case_uuid,
                  c.reference AS case_reference,
          c.type AS case_type,
          s.uuid::text as stage_uuid,
          s.type AS stage_type,
          s.user_uuid::text AS allocated_user_uuid,
          s.team_uuid::text AS team_uuid,
          c.owner_uuid::text AS owner_uuid,
          c.owner_team_uuid::text AS owner_team_uuid,
          c.created,
          c.date_received,
          c.case_deadline,
          DATE_PART('day', now() - c.created) as days_age,
          DATE_PART('day', c.case_deadline -now()) AS days_until_deadline
   FROM casework.case_data c
            JOIN casework.stage s ON s.case_uuid = c.uuid
   WHERE s.team_uuid IS NOT NULL AND c.deleted = false;
