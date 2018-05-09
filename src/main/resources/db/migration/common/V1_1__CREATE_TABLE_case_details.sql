CREATE TABLE IF NOT EXISTS case_details
(
  id               BIGSERIAL PRIMARY KEY,
  ref              BIGSERIAL,
  uuid             UUID,
  case_reference   VARCHAR(255) NOT NULL,
  case_type        VARCHAR(255) NOT NULL,
  stage            VARCHAR(255) NOT NULL,
  workflow_version INT,
  allocated_team   VARCHAR(255),
  allocated_user   VARCHAR(255),
  case_created     TIMESTAMP    NOT NULL,
  case_data        TEXT

);

CREATE TABLE IF NOT EXISTS rsh_case_details
(
  id             BIGSERIAL PRIMARY KEY,
  ref            BIGSERIAL,
  uuid           UUID,
  case_reference VARCHAR(255),
  leg_ref        VARCHAR(255),
  dob            DATE,
  forename       VARCHAR(255),
  surname        VARCHAR(255),
  case_type      VARCHAR(255) NOT NULL,
  case_created   TIMESTAMP    NOT NULL,
  last_modified  TIMESTAMP    NOT NULL,
  case_data      TEXT

);
