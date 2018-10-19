DROP TABLE IF EXISTS case_note cascade;

CREATE TABLE IF NOT EXISTS case_note
(
  id        BIGSERIAL PRIMARY KEY,
  uuid      UUID      NOT NULL,
  created   TIMESTAMP NOT NULL,
  case_uuid UUID      NOT NULL,
  text      TEXT      NOT NULL,
  type      TEXT      NOT NULL,
  deleted   BOOLEAN   NOT NULL DEFAULT FALSE,

  CONSTRAINT case_note_uuid_idempotent UNIQUE (uuid),
  CONSTRAINT fk_case_note_id FOREIGN KEY (case_uuid) REFERENCES case_data (uuid)
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