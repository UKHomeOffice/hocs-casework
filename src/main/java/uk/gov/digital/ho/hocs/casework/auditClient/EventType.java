package uk.gov.digital.ho.hocs.casework.auditClient;

import lombok.Getter;

public enum EventType {
    CASE_ALLOCATED_SELF,
    CASE_ALLOCATED_TO,
    CASE_CREATED,
    CASE_VIEWED;
}