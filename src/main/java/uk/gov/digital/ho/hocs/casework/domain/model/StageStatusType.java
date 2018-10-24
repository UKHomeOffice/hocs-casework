package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.Getter;

public enum StageStatusType {
    CREATED("CREATED"),
    ALLOCATED("ALLOCATED"),
    REJECTED("REJECTED"),
    COMPLETE("COMPLETE");

    @Getter
    private String displayValue;

    StageStatusType(String value) {
        displayValue = value;
    }
}
