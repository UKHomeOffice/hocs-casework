DROP TABLE IF EXISTS active_stage;

ALTER TABLE stage_data
  ADD COLUMN active boolean not null default TRUE;