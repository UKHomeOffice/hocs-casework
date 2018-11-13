DROP TABLE IF EXISTS case_note cascade;

CREATE TABLE IF NOT EXISTS case_note
(
  id        BIGSERIAL PRIMARY KEY,
  uuid      UUID      NOT NULL,
  created   TIMESTAMP NOT NULL,
  case_uuid UUID      NOT NULL,
  type      TEXT      NOT NULL,
  text      TEXT      NOT NULL,
  deleted   BOOLEAN   NOT NULL DEFAULT FALSE,

  CONSTRAINT case_note_uuid_idempotent UNIQUE (uuid),
  CONSTRAINT fk_case_note_id FOREIGN KEY (case_uuid) REFERENCES case_data (uuid)
);

CREATE INDEX case_note_case_uuid
  ON case_note (case_uuid, deleted);

CREATE OR REPLACE VIEW active_case_note AS
  SELECT c.*
  FROM case_note c
         JOIN active_case ac ON c.case_uuid = ac.uuid
  WHERE NOT c.deleted;

DROP TABLE IF EXISTS topic cascade;

CREATE TABLE IF NOT EXISTS topic
(
  id        BIGSERIAL PRIMARY KEY,
  uuid      UUID      NOT NULL,
  created   TIMESTAMP NOT NULL,
  case_uuid UUID      NOT NULL,
  text      TEXT,
  text_uuid UUID      NOT NULL,
  deleted   BOOLEAN   NOT NULL DEFAULT FALSE,

  CONSTRAINT topic_uuid_idempotent UNIQUE (uuid),
  CONSTRAINT fk_topic_id FOREIGN KEY (case_uuid) REFERENCES case_data (uuid)
);

CREATE INDEX topic_case_uuid
  ON case_note (case_uuid, deleted);

CREATE OR REPLACE VIEW active_topic AS
  SELECT t.*
  FROM topic t
         JOIN active_case ac ON t.case_uuid = ac.uuid
  WHERE NOT t.deleted;

CREATE OR REPLACE VIEW primary_topic AS
  SELECT t.*
  FROM topic t
         JOIN active_case ac ON t.uuid = ac.primary_topic_uuid
  WHERE NOT t.deleted;

DROP TABLE IF EXISTS correspondent cascade;

CREATE TABLE IF NOT EXISTS correspondent
(
  id        BIGSERIAL PRIMARY KEY,
  uuid      UUID      NOT NULL,
  created   TIMESTAMP NOT NULL,
  type      TEXT      NOT NULL,
  case_uuid UUID      NOT NULL,
  fullname  TEXT      NOT NULL,
  postcode  TEXT,
  address1  TEXT,
  address2  TEXT,
  address3  TEXT,
  country   TEXT,
  telephone TEXT,
  email     TEXT,
  reference TEXT      NOT NULL,
  deleted   BOOLEAN   NOT NULL DEFAULT FALSE,

  CONSTRAINT correspondent_uuid_idempotent UNIQUE (uuid),
  CONSTRAINT fk_correspondent_id FOREIGN KEY (case_uuid) REFERENCES case_data (uuid)
);

CREATE OR REPLACE VIEW active_correspondent AS
  SELECT c.*
  FROM correspondent c
         JOIN active_case ac ON c.case_uuid = ac.uuid
  WHERE NOT c.deleted;

CREATE OR REPLACE VIEW primary_correspondent AS
  SELECT c.*
  FROM correspondent c
         JOIN active_case ac ON c.uuid = ac.primary_correspondent_uuid
  WHERE NOT c.deleted;