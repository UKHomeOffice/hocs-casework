package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.Getter;

public enum CaseDataType {
    MIN("MIN"),
    TRO("TRO"),
    DTEN("DTEN");

    @Getter
    private String displayValue;

    CaseDataType(String value) {
        displayValue = value;
    }
}
