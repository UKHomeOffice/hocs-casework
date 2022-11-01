INSERT INTO case_data (uuid, created, type, reference, data, primary_topic_uuid, primary_correspondent_uuid,
                       case_deadline, date_received, deleted) VALUES
('fbdbaeab-6719-4e3a-a221-d061dde469a1', '2021-01-01 12:00:00', 'TEST', 'TEST/0000001/18', '{"DateReceived": "2021-01-01"}', null, null, '2021-01-29', '2021-01-01', false);

INSERT INTO case_data_tag (uuid, case_uuid, tag, created_at) VALUES
(gen_random_uuid(), 'fbdbaeab-6719-4e3a-a221-d061dde469a1', 'TEST_TAG', '2022-10-26 14:12:46.582090');
