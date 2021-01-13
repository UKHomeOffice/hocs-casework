CREATE OR REPLACE VIEW casework.correspondents_json_by_case AS
    SELECT case_uuid, json_build_object('correspondents', jsonb_agg(json_build_object('fullname', fullname, 'type', "type", 'is_primary', is_primary))) as correspondents
    FROM (
      SELECT fullname, case_uuid, correspondent.type,
		case
		 when correspondent.uuid = case_data.primary_correspondent_uuid then 'true'
		 else 'false'
	    end is_primary
		FROM casework.correspondent correspondent
		LEFT JOIN casework.case_data case_data ON correspondent.case_uuid = case_data.uuid
		WHERE correspondent.deleted = false
    ) correspondents_by_case
    GROUP BY case_uuid;
