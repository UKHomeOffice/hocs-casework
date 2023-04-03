DELETE FROM stage WHERE uuid IN (
    '0262dd31-19c3-4ee6-a009-a97240f68d28',
    'bd16c1d0-89ae-4f4d-84c6-8cf033480a0b',
    '9699bd15-7d47-4d7e-adb4-a5c166bf926a',
    '0e766733-ff0b-4d16-bcf8-3a721a5aa52a',
    '47223448-400d-4d48-bbc6-3670c828afd8',
    '88b124cf-7967-4a43-98a8-8968aac6605b'
);

UPDATE case_data SET primary_correspondent_uuid = NULL WHERE uuid IN (
    'a00f2b19-afc8-4e4e-9b9b-ead14eb8e026',
    'addf9415-bde8-465f-b5ac-e93f460f2ed3',
    '9e3392ff-b502-4352-8889-d6b1ce3e9a07',
    '6c9800d4-5cb2-409a-ab81-10cf862ae144',
    'e7051e71-0778-4197-a063-93c768c8a5ad',
    '1750a235-1157-4141-a270-a3f46d1db7fa'
);

DELETE FROM correspondent WHERE uuid IN (
    '7576fdf1-51f0-486b-9b2e-5f2fc17252d1',
    '38231fe6-488b-4dde-b0f8-33530522d80b',
    'bbc2bb1b-84d6-4187-bea3-4d338ae265be',
    'b40fc27d-bf86-46ac-946b-d17208301187',
    '7a79ba9d-6fb6-4776-bca7-2539a7184ff0',
    '16e671b4-b5d8-46f0-b864-9576229cdd5b'
);

DELETE FROM case_data WHERE uuid IN (
    'a00f2b19-afc8-4e4e-9b9b-ead14eb8e026',
    'addf9415-bde8-465f-b5ac-e93f460f2ed3',
    '9e3392ff-b502-4352-8889-d6b1ce3e9a07',
    '6c9800d4-5cb2-409a-ab81-10cf862ae144',
    'e7051e71-0778-4197-a063-93c768c8a5ad',
    '1750a235-1157-4141-a270-a3f46d1db7fa'
);
