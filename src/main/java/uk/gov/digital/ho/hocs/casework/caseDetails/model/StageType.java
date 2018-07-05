package uk.gov.digital.ho.hocs.casework.caseDetails.model;

import lombok.Getter;

public enum StageType {

    RUSH_ONLY_STAGE("Stage"),
    DCU_MIN_CATEGORISE("DCU_MIN_CATEGORISE");

    @Getter
    private String stringValue;

    StageType(String value){
        stringValue = value;
    }
}
