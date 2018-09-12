DROP TABLE IF EXISTS case_correspondent cascade;
Drop table if exists correspondent_data;

CREATE TABLE IF NOT EXISTS correspondent_data
(
  id        BIGSERIAL PRIMARY KEY,
  uuid      UUID      NOT NULL,
  fullname  TEXT      NOT NULL,
  postcode  TEXT,
  address1  TEXT,
  address2  TEXT,
  address3  TEXT,
  country   TEXT,
  telephone TEXT,
  email     TEXT,
  added     TIMESTAMP NOT NULL,
  updated   TIMESTAMP,
  CONSTRAINT correspondent_uuid_idempotent UNIQUE (uuid)

);


CREATE TABLE IF NOT EXISTS case_correspondent

(
  id                 BIGSERIAL PRIMARY KEY,
  case_uuid          UUID NOT NULL,
  correspondent_uuid UUID NOT NULL,
  type               TEXT NOT NULL,

  CONSTRAINT fk_case_id FOREIGN KEY (case_uuid) REFERENCES case_data (uuid),
  CONSTRAINT fk_correspondent_id FOREIGN KEY (correspondent_uuid) REFERENCES correspondent_data (uuid)
);
