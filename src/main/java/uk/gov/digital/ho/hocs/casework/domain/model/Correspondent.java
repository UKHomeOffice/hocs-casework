package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "correspondent")
public class Correspondent extends BaseCorrespondent {


    public Correspondent(UUID caseUUID,
                         String correspondentType,
                         String fullName,
                         String organisation,
                         Address address,
                         String telephone,
                         String email,
                         String reference,
                         String externalKey) {
        if (caseUUID == null || correspondentType == null) {
            throw new ApplicationExceptions.EntityCreationException(String.format("Cannot create Correspondent(%s, %s, %s, %s, %s, %s).", caseUUID, correspondentType, fullName, "Address", telephone, email), LogEvent.CORRESPONDENT_CREATE_FAILURE);
        }

        this.uuid = UUID.randomUUID();
        this.created = LocalDateTime.now();
        this.caseUUID = caseUUID;
        this.correspondentType = correspondentType;
        this.fullName = fullName;
        this.organisation = organisation;
        if (address != null) {
            this.postcode = address.getPostcode();
            this.address1 = address.getAddress1();
            this.address2 = address.getAddress2();
            this.address3 = address.getAddress3();
            this.country = address.getCountry();
        }
        this.telephone = telephone;
        this.email = email;
        this.reference = reference;
        this.externalKey = externalKey;
    }

}
