SET search_path TO casework;

CREATE TYPE BANK_HOLIDAY_REGION AS ENUM ('UNITED_KINGDOM', 'ENGLAND_AND_WALES', 'SCOTLAND', 'NORTHERN_IRELAND');

CREATE TABLE bank_holiday (
     id                          BIGSERIAL PRIMARY KEY,
     region                      BANK_HOLIDAY_REGION NOT NULL,
     date                        DATE NOT NULL,

     CONSTRAINT bank_holiday_uuid_idempotent UNIQUE (id),
     CONSTRAINT region_date_idempotent UNIQUE (region, date)
);
