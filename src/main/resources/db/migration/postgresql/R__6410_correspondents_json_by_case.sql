SET search_path TO casework;

DROP VIEW IF EXISTS correspondents_json_by_case;

create view correspondents_json_by_case(case_uuid, correspondents) as
SELECT correspondents_by_case.case_uuid,
       json_build_object('correspondents',
                         jsonb_agg(json_build_object('fullname', correspondents_by_case.fullname, 'postcode',
                                                     correspondents_by_case.postcode, 'type',
                                                     correspondents_by_case.type, 'is_primary',
                                                     correspondents_by_case.is_primary))) AS correspondents
FROM (SELECT correspondent.fullname,
             correspondent.postcode,
             correspondent.case_uuid,
             correspondent.type,
             CASE
                 WHEN correspondent.uuid = case_data.primary_correspondent_uuid THEN 'true'::text
                 ELSE 'false'::text
                 END AS is_primary
      FROM casework.correspondent correspondent
               LEFT JOIN casework.case_data case_data ON correspondent.case_uuid = case_data.uuid
      WHERE correspondent.deleted = false) correspondents_by_case
GROUP BY correspondents_by_case.case_uuid;

