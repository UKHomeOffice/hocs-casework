INSERT INTO case_data (uuid, created, type, reference, data, primary_topic_uuid, primary_correspondent_uuid,
                       case_deadline, date_received, deleted)
VALUES ('fbdbaeab-6719-4e3a-a221-d061dde469a1', '2018-12-14 12:00:00', 'TEST', 'TEST/9990191/18',
        '{"DateReceived": "2018-01-01"}', null, null, '2018-01-29', '2018-01-01', false);


INSERT INTO case_note (uuid, created, case_uuid, type, text, deleted, author)
VALUES ('a2bb3622-b38a-479d-b390-f633bf15f329', '2018-12-14 12:00:00', 'fbdbaeab-6719-4e3a-a221-d061dde469a1', 'TEST', 'a case note', false, 'User 1');
