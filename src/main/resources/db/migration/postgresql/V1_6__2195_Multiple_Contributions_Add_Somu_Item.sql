CREATE TABLE IF NOT EXISTS somu_item
(
  id                BIGSERIAL   PRIMARY KEY,
  uuid              UUID        NOT NULL,
  case_uuid         UUID        NOT NULL,
  somu_uuid         UUID        NOT NULL,
  data              JSONB       DEFAULT '{}',

  CONSTRAINT somu_item_uuid_unique UNIQUE (uuid),
  CONSTRAINT somu_item_case_uuid_sumo_uuid_unique UNIQUE (case_uuid, somu_uuid),
  CONSTRAINT fk_somu_item_case_uuid FOREIGN KEY (case_uuid) REFERENCES case_data(uuid)
);

CREATE INDEX IF NOT EXISTS idx_somu_item_uuid ON somu_item(case_uuid);
CREATE INDEX IF NOT EXISTS idx_somu_item_case_uuid ON somu_item(case_uuid);
CREATE INDEX IF NOT EXISTS idx_somu_item_case_uuid_type ON somu_item(case_uuid, somu_uuid);
