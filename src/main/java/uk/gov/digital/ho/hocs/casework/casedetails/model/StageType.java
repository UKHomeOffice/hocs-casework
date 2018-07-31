package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;

public enum StageType {

    RUSH_ONLY_STAGE("Stage"),
    DCU_MIN_MARKUP("DCU_MIN_MARKUP"),
    DCU_MIN_TRANSFER_CONFIRMATION("DCU_MIN_TRANSFER_CONFIRMATION"),
    UKVI_BREF_CATEGORISE("UKVI_BREF_CATEGORISE");

    @Getter
    private String stringValue;

    StageType(String value){
        stringValue = value;
    }
}
