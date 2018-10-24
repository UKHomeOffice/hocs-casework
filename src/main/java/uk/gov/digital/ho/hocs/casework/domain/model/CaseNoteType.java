package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.Getter;

public enum CaseNoteType {
    MANUAL("MANUAL");

    @Getter
    private String displayValue;

    CaseNoteType(String value) {
        displayValue = value;
    }
}
