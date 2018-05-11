CREATE TABLE IF NOT EXISTS rsh_case
(
  id        BIGSERIAL PRIMARY KEY,
  uuid      TEXT      NOT NULL,
  timestamp TIMESTAMP NOT NULL,
  action    TEXT      NOT NULL,
  data      JSONB     NOT NULL
);