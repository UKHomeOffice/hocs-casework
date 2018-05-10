CREATE TABLE IF NOT EXISTS case_details
(
  id               BIGSERIAL PRIMARY KEY,
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
