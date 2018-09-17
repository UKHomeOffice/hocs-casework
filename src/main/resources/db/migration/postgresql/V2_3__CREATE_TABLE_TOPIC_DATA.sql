DROP TABLE IF EXISTS topic_data cascade;

CREATE TABLE IF NOT EXISTS topic_data
(
  id         BIGSERIAL PRIMARY KEY,
  case_uuid  UUID      NOT NULL,
  topic_name TEXT,
  topic_uuid UUID      NOT NULL,
  created    TIMESTAMP NOT NULL,
  modified   TIMESTAMP,
  deleted    BOOLEAN   NOT NULL DEFAULT FALSE,

  CONSTRAINT fk_topic_id FOREIGN KEY (case_uuid) REFERENCES case_data (uuid)
);

CREATE INDEX idx_topic_uuid
  ON topic_data (topic_uuid);
CREATE INDEX idx_topic_name
  ON topic_data (topic_name);
