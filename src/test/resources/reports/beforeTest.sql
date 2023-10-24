INSERT INTO casework.case_data (uuid, created, type, reference, data, primary_topic_uuid, primary_correspondent_uuid, case_deadline, date_received, deleted, date_completed)
VALUES
    ('a00f2b19-afc8-4e4e-9b9b-ead14eb8e026', now() - INTERVAL '1 week',  'COMP', 'COMP/123450/23', '{"BusArea": "UKVI"}', NULL, NULL, now() - INTERVAL '1 week' + INTERVAL '30' DAY, now() - INTERVAL '1 week', FALSE, null),
    ('addf9415-bde8-465f-b5ac-e93f460f2ed3', now() - INTERVAL '2 week',  'COMP', 'COMP/123451/23', '{"BusArea": "UKVI"}', NULL, NULL, now() - INTERVAL '2 week' + INTERVAL '30' DAY, now() - INTERVAL '2 week', FALSE, now()),
    ('9e3392ff-b502-4352-8889-d6b1ce3e9a07', now() - INTERVAL '3 week',  'COMP2', 'COMP2/123452/23', '{"BusArea": "UKVI"}', NULL, NULL, now() - INTERVAL '3 week' + INTERVAL '30' DAY, now() - INTERVAL '3 week', FALSE, null),
    ('6c9800d4-5cb2-409a-ab81-10cf862ae144', now() - INTERVAL '4 week',  'COMP', 'COMP/123453/23', '{"BusArea": "UKVI"}', NULL, NULL, now() - INTERVAL '4 week' + INTERVAL '30' DAY, now() - INTERVAL '4 week', FALSE, null),
    ('e7051e71-0778-4197-a063-93c768c8a5ad', now() - INTERVAL '5 week',  'COMP', 'COMP/123454/23', '{"BusArea": "UKVI"}', NULL, NULL, now() - INTERVAL '5 week' + INTERVAL '30' DAY, now() - INTERVAL '5 week', FALSE, null),
    ('1750a235-1157-4141-a270-a3f46d1db7fa', now() - INTERVAL '6 week',  'COMP', 'COMP/123455/23', '{"BusArea": "UKVI"}', NULL, NULL, now() - INTERVAL '6 week' + INTERVAL '30' DAY, now() - INTERVAL '6 week', FALSE, null);

INSERT INTO casework.correspondent (uuid, created, type, case_uuid, fullname, postcode, address1, address2, address3, country, telephone, email, reference, deleted, external_key)
VALUES
    ('7576fdf1-51f0-486b-9b2e-5f2fc17252d1', now() - INTERVAL '1 week', 'Complainant', 'a00f2b19-afc8-4e4e-9b9b-ead14eb8e026', 'Bob Someone', 'S1 1DJ', '1 SomeWhere Close', 'Sheffield', 'South Yorkshire', 'England', '01142595959', 'a@a.com', '1', FALSE, 'external_key_1'),
    ('38231fe6-488b-4dde-b0f8-33530522d80b', now() - INTERVAL '2 week', 'Complainant', 'addf9415-bde8-465f-b5ac-e93f460f2ed3', 'Bob Someone', 'S1 1DJ', '1 SomeWhere Close', 'Sheffield', 'South Yorkshire', 'England', '01142595959', 'a@a.com', '1', FALSE, 'external_key_1'),
    ('bbc2bb1b-84d6-4187-bea3-4d338ae265be', now() - INTERVAL '3 week', 'Complainant', '9e3392ff-b502-4352-8889-d6b1ce3e9a07', 'Bob Someone', 'S1 1DJ', '1 SomeWhere Close', 'Sheffield', 'South Yorkshire', 'England', '01142595959', 'a@a.com', '1', FALSE, 'external_key_1'),
    ('b40fc27d-bf86-46ac-946b-d17208301187', now() - INTERVAL '4 week', 'Complainant', '6c9800d4-5cb2-409a-ab81-10cf862ae144', 'Bob Someone', 'S1 1DJ', '1 SomeWhere Close', 'Sheffield', 'South Yorkshire', 'England', '01142595959', 'a@a.com', '1', FALSE, 'external_key_1'),
    ('7a79ba9d-6fb6-4776-bca7-2539a7184ff0', now() - INTERVAL '5 week', 'Complainant', 'e7051e71-0778-4197-a063-93c768c8a5ad', 'Bob Someone', 'S1 1DJ', '1 SomeWhere Close', 'Sheffield', 'South Yorkshire', 'England', '01142595959', 'a@a.com', '1', FALSE, 'external_key_1'),
    ('16e671b4-b5d8-46f0-b864-9576229cdd5b', now() - INTERVAL '6 week', 'Complainant', '1750a235-1157-4141-a270-a3f46d1db7fa', 'Bob Someone', 'S1 1DJ', '1 SomeWhere Close', 'Sheffield', 'South Yorkshire', 'England', '01142595959', 'a@a.com', '1', FALSE, 'external_key_1');

UPDATE casework.case_data SET primary_correspondent_uuid = '7576fdf1-51f0-486b-9b2e-5f2fc17252d1' WHERE uuid = 'a00f2b19-afc8-4e4e-9b9b-ead14eb8e026';
UPDATE casework.case_data SET primary_correspondent_uuid = '38231fe6-488b-4dde-b0f8-33530522d80b' WHERE uuid = 'addf9415-bde8-465f-b5ac-e93f460f2ed3';
UPDATE casework.case_data SET primary_correspondent_uuid = 'bbc2bb1b-84d6-4187-bea3-4d338ae265be' WHERE uuid = '9e3392ff-b502-4352-8889-d6b1ce3e9a07';
UPDATE casework.case_data SET primary_correspondent_uuid = 'b40fc27d-bf86-46ac-946b-d17208301187' WHERE uuid = '6c9800d4-5cb2-409a-ab81-10cf862ae144';
UPDATE casework.case_data SET primary_correspondent_uuid = '7a79ba9d-6fb6-4776-bca7-2539a7184ff0' WHERE uuid = 'e7051e71-0778-4197-a063-93c768c8a5ad';
UPDATE casework.case_data SET primary_correspondent_uuid = '16e671b4-b5d8-46f0-b864-9576229cdd5b' WHERE uuid = '1750a235-1157-4141-a270-a3f46d1db7fa';

INSERT INTO casework.stage (uuid, created, type, deadline, case_uuid, team_uuid, user_uuid)
VALUES
    ('0262dd31-19c3-4ee6-a009-a97240f68d28', now() - INTERVAL '1 week', 'TEST_STAGE_1', now() - INTERVAL '1 week' + INTERVAL '30' DAY, 'a00f2b19-afc8-4e4e-9b9b-ead14eb8e026', '6761deeb-950a-4735-8b4c-6c6708d3153f', 'ed6e908d-ecb1-4d40-861f-742ab21b506c'),
    ('bd16c1d0-89ae-4f4d-84c6-8cf033480a0b', now() - INTERVAL '2 week', 'TEST_STAGE_2', now() - INTERVAL '2 week' + INTERVAL '30' DAY, 'addf9415-bde8-465f-b5ac-e93f460f2ed3', 'e4ed4136-6dde-448e-a810-94cc2fc18097', NULL),
    ('9699bd15-7d47-4d7e-adb4-a5c166bf926a', now() - INTERVAL '3 week', 'TEST_STAGE_1', now() - INTERVAL '3 week' + INTERVAL '30' DAY, '9e3392ff-b502-4352-8889-d6b1ce3e9a07', '6761deeb-950a-4735-8b4c-6c6708d3153f', '212db2a6-c61d-47cc-98b8-09e0cd19a755'),
    ('0e766733-ff0b-4d16-bcf8-3a721a5aa52a', now() - INTERVAL '4 week', 'TEST_STAGE_3', now() - INTERVAL '4 week' + INTERVAL '30' DAY, '6c9800d4-5cb2-409a-ab81-10cf862ae144', 'e4ed4136-6dde-448e-a810-94cc2fc18097', '3eabb8f7-8084-4ab1-aefe-cbd6f3063fbb'),
    ('47223448-400d-4d48-bbc6-3670c828afd8', now() - INTERVAL '5 week', 'TEST_STAGE_1', now() - INTERVAL '5 week' + INTERVAL '30' DAY, 'e7051e71-0778-4197-a063-93c768c8a5ad', '6761deeb-950a-4735-8b4c-6c6708d3153f', '8da250c9-1028-4b08-9c8b-fb413da7b287'),
    ('88b124cf-7967-4a43-98a8-8968aac6605b', now() - INTERVAL '6 week', 'TEST_STAGE_0', now() - INTERVAL '6 week' + INTERVAL '30' DAY, '1750a235-1157-4141-a270-a3f46d1db7fa', '3525de88-a684-4d33-9cca-14ead39acf19', '3eabb8f7-8084-4ab1-aefe-cbd6f3063fbb');

