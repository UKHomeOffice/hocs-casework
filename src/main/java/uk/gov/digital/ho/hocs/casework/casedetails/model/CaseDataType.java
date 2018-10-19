package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;

public enum CaseDataType {
    MIN("Ministerial"),
    TRO("Treat Official"),
    DTEN("Number 10");

    @Getter
    private String displayValue;

    CaseDataType(String value) {
        displayValue = value;
    }
}
