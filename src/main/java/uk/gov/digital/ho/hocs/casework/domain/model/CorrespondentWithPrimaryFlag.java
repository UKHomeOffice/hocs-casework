package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CorrespondentWithPrimaryFlag extends Correspondent {

    @Getter
    private Boolean isPrimary;

    public CorrespondentWithPrimaryFlag(
            Correspondent correspondent,
            Boolean isPrimary
    ) {
        this.uuid = correspondent.getUuid();
        this.created = correspondent.created;
        this.caseUUID = correspondent.caseUUID;
        this.correspondentType = correspondent.correspondentType;
        this.fullName = correspondent.fullName;
        this.organisation = correspondent.organisation;
        this.postcode = correspondent.getPostcode();
        this.address1 = correspondent.getAddress1();
        this.address2 = correspondent.getAddress2();
        this.address3 = correspondent.getAddress3();
        this.country = correspondent.getCountry();
        this.telephone = correspondent.telephone;
        this.email = correspondent.email;
        this.reference = correspondent.reference;
        this.externalKey = correspondent.externalKey;

        this.isPrimary = isPrimary;
    }

}
