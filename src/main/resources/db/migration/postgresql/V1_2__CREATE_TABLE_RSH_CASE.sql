CREATE TABLE IF NOT EXISTS rsh_case
(
  id        BIGSERIAL PRIMARY KEY,
  type      TEXT      NOT NULL,
  reference TEXT      NOT NULL,
  uuid      TEXT      NOT NULL,
  created   TIMESTAMP NOT NULL,
  data      JSONB     NOT NULL
);