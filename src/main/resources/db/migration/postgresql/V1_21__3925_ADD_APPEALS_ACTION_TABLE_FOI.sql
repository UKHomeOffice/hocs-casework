SET search_path TO casework;

DROP TABLE IF EXISTS action_data_appeals;

CREATE OR REPLACE FUNCTION update_last_updated_timestamp_on_data_change()
    RETURNS TRIGGER AS $$
BEGIN
    IF (tg_op = 'INSERT') OR row(NEW.*) IS DISTINCT FROM row(OLD.*) THEN
        NEW.last_updated_timestamp = now();
        RETURN NEW;
    ELSE
        RETURN OLD;
    END IF;
END;
$$ language 'plpgsql';

CREATE TABLE action_data_appeals (
    uuid                        UUID PRIMARY KEY,
    action_uuid                 UUID NOT NULL, -- knitted to info.schema case_type_action table entry uuid.
    action_label                TEXT NOT NULL,
    case_data_type              TEXT NOT NULL,
    case_data_uuid              UUID NOT NULL, -- fk case_data.uuid
    status                      TEXT NOT NULL,
    date_sent_rms               TIMESTAMP WITHOUT TIME ZONE,
    outcome                     TEXT,
    complex_case                BOOLEAN,
    note                        TEXT DEFAULT '',
    appeal_officer_data         jsonb,
    created_timestamp           TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
    last_updated_timestamp      TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT action_data_appeals_uuid_idempotent UNIQUE (uuid),
    CONSTRAINT fk_case_uuid FOREIGN KEY (case_data_uuid) REFERENCES case_data(uuid)
);

CREATE TRIGGER action_data_appeals_last_updated_timestamp
    BEFORE INSERT OR UPDATE ON action_data_appeals FOR EACH ROW EXECUTE PROCEDURE update_last_updated_timestamp_on_data_change();

DROP TABLE IF EXISTS action_data_extensions;

CREATE TABLE action_data_extensions (
    uuid                        UUID PRIMARY KEY,
    action_uuid                 UUID NOT NULL, -- knitted to info.schema case_type_action table entry uuid.
    action_label                TEXT NOT NULL,
    case_data_type              TEXT NOT NULL,
    case_data_uuid              UUID NOT NULL, -- fk case_data.uuid
    original_deadline           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_deadline            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    note                        TEXT DEFAULT '',
    created_timestamp           TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
    last_updated_timestamp      TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT action_data_extensions_uuid_idempotent UNIQUE (uuid),
    CONSTRAINT fk_case_uuid FOREIGN KEY (case_data_uuid) REFERENCES case_data(uuid)
);

CREATE TRIGGER action_data_extensions_last_updated_timestamp
    BEFORE INSERT OR UPDATE ON action_data_appeals FOR EACH ROW EXECUTE PROCEDURE update_last_updated_timestamp_on_data_change();

-- Remove old implementation... originally for FOI however replaced before release.
-- DROP TABLE IF EXISTS case_deadline_extension;