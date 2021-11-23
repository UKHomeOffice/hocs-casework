DROP INDEX IF EXISTS idx_somu_item_uuid;
DROP INDEX IF EXISTS idx_somu_item_case_uuid;

CREATE INDEX IF NOT EXISTS idx_somu_item_uuid ON somu_item(uuid);
