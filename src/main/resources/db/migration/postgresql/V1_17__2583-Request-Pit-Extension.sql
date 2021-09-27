DROP TABLE IF EXISTS case_deadline_extension_type;

CREATE TABLE IF NOT EXISTS case_deadline_extension_type
(
    type                       VARCHAR(32)  PRIMARY KEY,
    working_days                INTEGER      NOT NULL,

    CONSTRAINT case_deadline_type_unique UNIQUE (type)
);

INSERT INTO case_deadline_extension_type (type, working_days) VALUES ('FOI_PIT', 20);

DROP TABLE IF EXISTS case_deadline_extension;

CREATE TABLE IF NOT EXISTS case_deadline_extension
(
    id                         BIGSERIAL    PRIMARY KEY,
    type                       VARCHAR(32)  NOT NULL,
    case_uuid                  UUID         NOT NULL,


    CONSTRAINT fk_extension_type FOREIGN KEY (type) REFERENCES case_deadline_extension_type(type),
    CONSTRAINT fk_case_uuid FOREIGN KEY (case_uuid) REFERENCES case_data (uuid)
);
