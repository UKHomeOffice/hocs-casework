package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;

public enum ReferenceType {

   MP("MP"),
   HO_REFERENCE("HO Reference"),
   PASSPORT_NUMBER("Passport Number"),
   CID_NUMBER("CID Number");

    @Getter
    private String displayValue;

    ReferenceType(String value){
        displayValue = value;
    }
}
