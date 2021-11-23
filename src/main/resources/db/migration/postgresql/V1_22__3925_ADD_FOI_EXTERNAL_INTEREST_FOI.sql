CREATE TABLE action_data_external_interest (
    uuid                        UUID PRIMARY KEY,
    action_uuid                 UUID NOT NULL, -- knitted to info.schema case_type_action table entry uuid.
    action_label                TEXT NOT NULL,
    case_data_type              TEXT NOT NULL,
    party_type                  TEXT NOT NULL, -- the interested party type from the info.entity
    details_of_interest         TEXT,
    case_data_uuid              UUID NOT NULL, -- fk case_data.uuid
    created_timestamp           TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
    last_updated_timestamp      TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT action_data_external_interest_uuid_idempotent UNIQUE (uuid),
    CONSTRAINT fk_case_uuid FOREIGN KEY (case_data_uuid) REFERENCES case_data(uuid)
);

CREATE TRIGGER action_data_external_interest_last_updated_timestamp
    BEFORE INSERT OR UPDATE ON action_data_appeals FOR EACH ROW EXECUTE PROCEDURE update_last_updated_timestamp_on_data_change();
