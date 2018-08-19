DROP TABLE IF EXISTS stage_data;

CREATE TABLE IF NOT EXISTS stage_data
(
  id        BIGSERIAL PRIMARY KEY,
  uuid      UUID      NOT NULL,
  type      TEXT      NOT NULL,
  case_uuid UUID      NOT NULL,
  created   TIMESTAMP NOT NULL,
  updated   TIMESTAMP,
  active    BOOLEAN   NOT NULL DEFAULT TRUE,
  team_uuid UUID      NOT NULL,
  user_uuid UUID,

  CONSTRAINT stage_id_idempotent UNIQUE (uuid),
  CONSTRAINT fk_stage_id FOREIGN KEY (case_uuid) REFERENCES case_data (uuid)
);