package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;

public enum CaseNoteType {
    MANUAL("Manual");

    @Getter
    private String displayValue;

    CaseNoteType(String value) {
        displayValue = value;
    }
}
