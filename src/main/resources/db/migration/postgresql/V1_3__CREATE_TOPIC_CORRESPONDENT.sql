DROP TABLE IF EXISTS topic cascade;

CREATE TABLE IF NOT EXISTS topic
(
  id         BIGSERIAL PRIMARY KEY,
  uuid       UUID      NOT NULL,
  created    TIMESTAMP NOT NULL,
  case_uuid  UUID      NOT NULL,
  topic_name TEXT,
  topic_uuid UUID      NOT NULL,
  deleted    BOOLEAN   NOT NULL DEFAULT FALSE,

  CONSTRAINT topic_uuid_idempotent UNIQUE (uuid),
  CONSTRAINT fk_topic_id FOREIGN KEY (case_uuid) REFERENCES case_data (uuid)
);

DROP TABLE IF EXISTS correspondent cascade;

CREATE TABLE IF NOT EXISTS correspondent
(
  id        BIGSERIAL PRIMARY KEY,
  uuid      UUID      NOT NULL,
  created   TIMESTAMP NOT NULL,
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
  type      TEXT      NOT NULL,
  deleted   BOOLEAN   NOT NULL DEFAULT FALSE,

  CONSTRAINT correspondent_uuid_idempotent UNIQUE (uuid),
  CONSTRAINT fk_correspondent_id FOREIGN KEY (case_uuid) REFERENCES case_data (uuid)
);

DROP TABLE IF EXISTS reference cascade;

CREATE TABLE IF NOT EXISTS reference
(
  id        BIGSERIAL PRIMARY KEY,
  uuid      UUID      NOT NULL,
  created   TIMESTAMP NOT NULL,
  case_uuid UUID      NOT NULL,
  reference TEXT      NOT NULL,
  type      TEXT      NOT NULL,
  deleted   BOOLEAN   NOT NULL DEFAULT FALSE,

  CONSTRAINT reference_uuid_idempotent UNIQUE (uuid),
  CONSTRAINT fk_reference_id FOREIGN KEY (case_uuid) REFERENCES case_data (uuid)
);