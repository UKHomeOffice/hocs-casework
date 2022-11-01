SET search_path TO casework;

CREATE TABLE IF NOT EXISTS case_data_tag (
    uuid UUID NOT NULL,
    case_uuid UUID NOT NULL ,
    tag VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    deleted_on TIMESTAMP,

    PRIMARY KEY(uuid),
    CONSTRAINT fk_case_uuid FOREIGN KEY (case_uuid) REFERENCES case_data(uuid),
    CONSTRAINT case_uuid_tag_deleted_on_idempotent UNIQUE (case_uuid, tag, deleted_on)
);

CREATE INDEX IF NOT EXISTS idx_case_data_uuid ON case_data_tag USING btree(case_uuid) WHERE deleted_on IS NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_case_data_uuid_tag ON case_data_tag USING btree(case_uuid, tag) WHERE deleted_on IS NULL;
