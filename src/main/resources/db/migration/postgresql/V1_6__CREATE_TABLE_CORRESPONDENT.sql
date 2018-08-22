DROP TABLE IF EXISTS correspondent_data cascade;

CREATE TABLE IF NOT EXISTS correspondent_data
(
  id         BIGSERIAL PRIMARY KEY,
  uuid       UUID      NOT NULL,
  title      TEXT,
  first_name TEXT      NOT NULL,
  surname    TEXT      NOT NULL,
  postcode   TEXT,
  address1   TEXT,
  address2   TEXT,
  address3   TEXT,
  country    TEXT,
  telephone  TEXT,
  email      TEXT,
  added      TIMESTAMP NOT NULL,
  updated    TIMESTAMP
);

DROP TABLE IF EXISTS case_correspondent cascade;

CREATE TABLE IF NOT EXISTS case_correspondent

(
  id                 BIGSERIAL PRIMARY KEY,
  case_uuid          UUID NOT NULL,
  correspondent_uuid UUID NOT NULL,
  type               TEXT NOT NULL
);


