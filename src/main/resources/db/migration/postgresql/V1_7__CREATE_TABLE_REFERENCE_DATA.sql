DROP TABLE IF EXISTS reference_data cascade;

CREATE TABLE IF NOT EXISTS reference_data
(
  id             BIGSERIAL PRIMARY KEY,
  case_uuid      UUID NOT NULL,
  type TEXT NOT NULL,
  reference      TEXT NOT NULL,

  CONSTRAINT fk_reference_id FOREIGN KEY (case_uuid) REFERENCES case_data (uuid)
);



