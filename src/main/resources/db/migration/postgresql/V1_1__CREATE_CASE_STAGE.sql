DROP SEQUENCE IF EXISTS case_ref;

CREATE SEQUENCE case_ref
  START 0120001;

DROP TABLE IF EXISTS case_data;

CREATE TABLE IF NOT EXISTS case_data
(
  id                         BIGSERIAL PRIMARY KEY,
  uuid                       UUID        NOT NULL,
  created                    TIMESTAMP   NOT NULL,
  type                       VARCHAR(5)  NOT NULL,
  reference                  VARCHAR(16) NOT NULL,
  priority                   BOOLEAN     NOT NULL DEFAULT FALSE,
  data                       JSONB,
  primary_topic_uuid         UUID,
  primary_correspondent_uuid UUID,
  case_deadline              DATE        NOT NULL,
  date_received              DATE        NOT NULL,
  deleted                    BOOLEAN     NOT NULL DEFAULT FALSE,

  CONSTRAINT case_uuid_idempotent UNIQUE (uuid),
  CONSTRAINT case_ref_idempotent UNIQUE (reference)
);

--  GetCase + Used as index where we check cases aren't deleted
CREATE INDEX case_data_uuid
  ON case_data (uuid, deleted);

-- Used as index in build Primary_Topic view
CREATE INDEX case_data_topic
  ON case_data (primary_topic_uuid, deleted);

-- Used as index in build Primary_Correspondent view
CREATE INDEX case_data_correspondent
  ON case_data (primary_correspondent_uuid, deleted);

-- This should be used to ensure we only return results from cases we haven't deleted
CREATE OR REPLACE VIEW active_case AS
SELECT *
FROM case_data
WHERE NOT deleted;

DROP TABLE IF EXISTS stage;

CREATE TABLE IF NOT EXISTS stage
(
  id        BIGSERIAL PRIMARY KEY,
  uuid      UUID        NOT NULL,
  created   TIMESTAMP   NOT NULL,
  type      TEXT        NOT NULL,
  deadline  DATE,
  status    VARCHAR(16) NOT NULL,
  case_uuid UUID        NOT NULL,
  team_uuid UUID,
  user_uuid UUID,

  CONSTRAINT stage_id_idempotent UNIQUE (uuid),
  CONSTRAINT fk_stage_id FOREIGN KEY (case_uuid) REFERENCES case_data (uuid)
);

-- GetStage
CREATE INDEX stage_case_uuid_complete
  ON stage (uuid, case_uuid)
  WHERE status <> 'COMPLETED';

-- Workstacks
CREATE INDEX stage_team_uuid_complete
  ON stage (team_uuid, case_uuid)
  WHERE status <> 'COMPLETED';
CREATE INDEX stage_user_uuid_complete
  ON stage (user_uuid, case_uuid)
  WHERE status <> 'COMPLETED';

CREATE OR REPLACE VIEW stage_data AS
  SELECT c.reference AS case_reference, c.type AS case_type, c.data as data, s.*
  FROM stage s
         JOIN active_case c ON s.case_uuid = c.uuid;

CREATE OR REPLACE VIEW active_stage AS
  SELECT s.*
  FROM stage_data s
  WHERE s.status <> 'COMPLETED';

