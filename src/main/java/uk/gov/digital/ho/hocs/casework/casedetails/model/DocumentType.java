package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;

public enum DocumentType {
    ORIGINAL("Original"),
    DRAFT("Draft");

    @Getter
    private String displayValue;

    DocumentType(String value) {
        displayValue = value;
    }
}
