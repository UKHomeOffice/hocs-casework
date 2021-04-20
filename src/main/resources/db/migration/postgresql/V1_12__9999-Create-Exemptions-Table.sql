DROP TABLE IF EXISTS exemption cascade;

CREATE TABLE IF NOT EXISTS exemption
(
  id        BIGSERIAL PRIMARY KEY,
  uuid      UUID      NOT NULL,
  created   TIMESTAMP NOT NULL,
  type      TEXT      NOT NULL,
  case_uuid UUID      NOT NULL,
  deleted   BOOLEAN   NOT NULL DEFAULT FALSE,

  CONSTRAINT exemption_uuid_idempotent UNIQUE (uuid),
  CONSTRAINT fk_correspondent_id FOREIGN KEY (case_uuid) REFERENCES case_data (uuid)
);