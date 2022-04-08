SET search_path TO casework;

CREATE TABLE action_data_suspensions (
    uuid                        UUID PRIMARY KEY,
    action_uuid                 UUID NOT NULL, -- knitted to info.schema case_type_action table entry uuid.
    action_label                TEXT NOT NULL,
    action_subtype              TEXT NOT NULL,
    case_data_type              TEXT NOT NULL,
    case_data_uuid              UUID NOT NULL, -- fk case_data.uuid
    date_suspension_applied     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    date_suspension_removed     TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT action_data_suspensions_uuid_idempotent UNIQUE (uuid),
    CONSTRAINT fk_case_uuid FOREIGN KEY (case_data_uuid) REFERENCES case_data(uuid)
);

