INSERT INTO case_data (
    uuid,
    created,
    type,
    reference,
    data,
    primary_topic_uuid,
    primary_correspondent_uuid,
    case_deadline,
    date_received,
    deleted,
    completed,
    case_deadline_warning,
    date_completed,
    migrated_reference
) VALUES (
   '85492dfa-6642-4eee-9513-700b4bf4de8b',
   '2020-01-02 00:00:00',
   'COMP',
   'COMP/12345678/20',
   '{"ExistingField1": "ExistingValue1", "ExistingField2": "ExistingValue2"}',
   NULL,
   NULL,
   '2020-01-30',
   '2020-01-01 00:00:00',
   FALSE,
   TRUE,
   NULL,
   '2021-02-01 00:00:00',
   'ClosedMigratedRef123'
), (
   'b81482e9-4822-4792-9773-2d4a22b923e0',
   '2020-01-02 00:00:00',
   'COMP',
   'COMP/87654321/20',
   '{"ExistingField1": "ExistingValue1", "ExistingField2": "ExistingValue2"}',
   NULL,
   NULL,
   '2020-01-30',
   '2020-01-01 00:00:00',
   FALSE,
   FALSE,
   NULL,
   NULL,
   'OpenMigratedRef123'
), (
   'e69a1e91-885e-4bf4-a2d4-4af90cd8e475',
   '2020-01-02 00:00:00',
   'COMP',
   'COMP/12312345/20',
   '{"ExistingField1": "ExistingValue1", "ExistingField2": "ExistingValue2"}',
   NULL,
   NULL,
   '2020-01-30',
   '2020-01-01 00:00:00',
   FALSE,
   FALSE,
   NULL,
   NULL,
   'MissingStageMigratedRef123'
);

INSERT INTO stage (
    uuid, created, type, deadline, transition_note_uuid, case_uuid, team_uuid, user_uuid, deadline_warning, somu)
VALUES (
   'c0b3dd14-f59c-4bd7-bb8f-870d17c8a54a',
   '2021-02-01 00:00:00',
   'MIGRATION_COMP_CASE_CLOSED',
   '2020-01-30',
   NULL,
   '85492dfa-6642-4eee-9513-700b4bf4de8b',
   NULL,
   NULL,
   NULL,
   NULL
), (
   '05c82f2a-26f4-458c-8132-a33d2449db2c',
   '2020-01-01 00:00:00',
   'COMP_REGISTRATION',
   '2020-01-30',
   NULL,
   'b81482e9-4822-4792-9773-2d4a22b923e0',
   NULL,
   NULL,
   NULL,
   NULL
), (
   '9658f450-0786-4a66-8dea-23adb7484795',
   '2023-06-30 00:00:00',
   'COMP_SERVICE_TRIAGE',
   '2020-01-30',
   NULL,
   'b81482e9-4822-4792-9773-2d4a22b923e0',
   '69ab5756-3a75-46be-95ba-8b5720aee111',
   NULL,
   NULL,
   NULL
);
