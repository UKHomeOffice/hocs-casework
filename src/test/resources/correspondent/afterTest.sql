DELETE FROM casework.correspondent WHERE uuid IN (
'cb4af956-4946-4a2d-9bd6-aee429df7396',
'64ffd23c-2f3c-44b4-84ee-5b2ce0eafc40',
'9ded5e1c-f7f7-4930-bc19-f56518746721',
'c09d94ab-8156-49d6-b7f1-758637c7c048',
'bc33ca22-77c0-451a-a896-950d01005c97',
'457e967c-a194-4f15-a0c8-9ff4ce0b436a',
'0885df7e-aeec-4fc8-a726-767749359736'
);

DELETE FROM casework.case_data WHERE uuid IN (
    '278b3ba0-12d3-41f9-8cd0-cb2f8e0075ef',
    'e9018595-3591-4901-89e8-e6e5b41bddd6',
    'a21cf9a3-2150-4c2c-94d0-1e33e5b2c5b5',
    'deae1f34-e819-4564-80fd-6b984656f392',
    '479644e1-fff9-4163-a37f-fc85e157d17a'
);
