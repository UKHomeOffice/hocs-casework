INSERT INTO case_data (uuid, created, date_received, reference, case_deadline, type, deleted)
VALUES
    ('278b3ba0-12d3-41f9-8cd0-cb2f8e0075ef', now(), now(), 'TEST/01234567/23', now(), 'TEST', false),
    ('e9018595-3591-4901-89e8-e6e5b41bddd6', now(), now(), 'TEST/01234568/23', now(), 'TEST', false),
    ('a21cf9a3-2150-4c2c-94d0-1e33e5b2c5b5', now(), now(), 'TEST/01234569/23', now(), 'TEST', false),
    ('deae1f34-e819-4564-80fd-6b984656f392', now(), now(), 'TEST/01234570/23', now(), 'TEST', false),
    ('479644e1-fff9-4163-a37f-fc85e157d17a', now(), now(), 'TEST/01234571/23', now(), 'TEST', true)
ON CONFLICT DO NOTHING;


INSERT INTO casework.correspondent (uuid, case_uuid, fullname, deleted, type, created)
VALUES
    ('cb4af956-4946-4a2d-9bd6-aee429df7396', '278b3ba0-12d3-41f9-8cd0-cb2f8e0075ef', 'ActiveOne CaseOne', false, 'TEST', now()),
    ('64ffd23c-2f3c-44b4-84ee-5b2ce0eafc40', 'e9018595-3591-4901-89e8-e6e5b41bddd6', 'ActiveOne CaseTwo', false, 'TEST', now()),
    ('9ded5e1c-f7f7-4930-bc19-f56518746721', 'e9018595-3591-4901-89e8-e6e5b41bddd6', 'ActiveTwo CaseTwo', false, 'TEST', now()),
    ('c09d94ab-8156-49d6-b7f1-758637c7c048', 'a21cf9a3-2150-4c2c-94d0-1e33e5b2c5b5', 'ActiveOne CaseThree', false, 'TEST', now()),
    ('bc33ca22-77c0-451a-a896-950d01005c97', 'a21cf9a3-2150-4c2c-94d0-1e33e5b2c5b5', 'DeletedOne CaseThree', true, 'TEST', now()),
    ('457e967c-a194-4f15-a0c8-9ff4ce0b436a', 'deae1f34-e819-4564-80fd-6b984656f392', 'DeletedOne CaseFour', true, 'TEST', now()),
    ('0885df7e-aeec-4fc8-a726-767749359736', '479644e1-fff9-4163-a37f-fc85e157d17a', 'ActiveOne DeletedCase', false, 'TEST', now())
ON CONFLICT DO NOTHING;
