package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.Getter;

public enum StageStatusType {
    UNASSIGNED("UNASSIGNED"),
    TEAM_ASSIGNED("TEAM_ASSIGNED"),
    USER_ASSIGNED("USER_ASSIGNED"),
    UPDATED("UPDATED"),
    REJECTED("REJECTED"),
    COMPLETED("COMPLETED");

    @Getter
    private String displayValue;

    StageStatusType(String value) {
        displayValue = value;
    }
}
