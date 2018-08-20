DROP TABLE IF EXISTS deadline_data cascade;

CREATE TABLE IF NOT EXISTS deadline_data
(
  id        BIGSERIAL PRIMARY KEY,
  case_uuid UUID      NOT NULL,
  stage     TEXT      NOT NULL,
  date  DATE NOT NULL,

  CONSTRAINT fk_deadline_id FOREIGN KEY (case_uuid) REFERENCES case_data (uuid)
);

CREATE INDEX idx_deadline_date  ON deadline_data (date);
CREATE INDEX idx_deadline_case_uuid  ON deadline_data (case_uuid);
