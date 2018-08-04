package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;

public enum CaseType {
    RSH("WR Response"),
    MIN("Ministerial");

    @Getter
    private String displayValue;

    CaseType(String value) {
        displayValue = value;
    }
}
