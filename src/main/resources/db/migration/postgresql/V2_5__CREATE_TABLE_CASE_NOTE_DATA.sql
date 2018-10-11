DROP TABLE IF EXISTS case_note_data cascade;

CREATE TABLE IF NOT EXISTS case_note_data
(
  id        BIGSERIAL PRIMARY KEY,
  uuid      UUID      NOT NULL,
  case_uuid UUID      NOT NULL,
  case_note TEXT      NOT NULL,
  created   TIMESTAMP NOT NULL,
  deleted   BOOLEAN   NOT NULL DEFAULT FALSE,

  CONSTRAINT fk_case_note_id FOREIGN KEY (case_uuid) REFERENCES case_data (uuid),
  CONSTRAINT uuid_idempotent UNIQUE (uuid)
);

