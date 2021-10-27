SET search_path TO casework;

INSERT INTO case_data (uuid, created, type, reference, data, primary_topic_uuid, primary_correspondent_uuid,
                       case_deadline, date_received, deleted)
VALUES ('14915b78-6977-42db-b343-0915a7f412a1', '2018-12-14 12:00:00', 'TEST', 'TEST/9990190/18',
        '{"DateReceived": "2018-01-01"}', null, null, '2018-01-29', '2018-01-01', false) ON CONFLICT DO NOTHING;

INSERT INTO stage (uuid, created, type, deadline, case_uuid, team_uuid, user_uuid)
VALUES ('e9151b83-7602-4419-be83-bff1c924c80d', '2018-12-14 12:00:00', 'INITIAL_DRAFT', '2018-01-07',
        '14915b78-6977-42db-b343-0915a7f412a1', '20a47a36-361f-4b85-90e3-1a1946874ef6',
        '4035d37f-9c1d-436e-99de-1607866634d4') ON CONFLICT DO NOTHING;

INSERT INTO correspondent (uuid, created, type, case_uuid, fullname, postcode, address1, address2, address3, country,
                           telephone, email, reference, deleted)
VALUES ('2c9e1eb9-ee78-4f57-a626-b8b75cf3b937', '2018-12-14 12:00:00', 'Member', '14915b78-6977-42db-b343-0915a7f412a1',
        'Bob Someone', 'S1 1DJ', '1 SomeWhere Close', 'Sheffield', 'South Yorkshire', 'England', '01142595959',
        'a@a.com', '1', false) ON CONFLICT DO NOTHING;

INSERT INTO action_data_appeals (uuid, action_uuid, action_label, case_data_type, case_data_uuid, status, date_sent_rms,outcome,complex_case, note,appeal_officer_data)
VALUES (
        'd159c936-b727-464e-a0ed-b63134fe0b37',
        '326eddb3-ba64-4253-ad39-916ccbb59f4e',
        'IR APPEAL',
        'FOI',
        '14915b78-6977-42db-b343-0915a7f412a1',
        'Pending',
        null,
        null,
        null,
        null,
        '{}'::jsonb
       ) ON CONFLICT DO NOTHING;