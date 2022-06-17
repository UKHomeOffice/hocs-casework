package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "correspondent")
public class Correspondent implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Getter
    @Column(name = "uuid")
    protected UUID uuid;

    @Setter
    @Getter
    @Column(name = "created")
    protected LocalDateTime created;

    @Getter
    @Column(name = "type")
    protected String correspondentType;

    @Getter
    @Setter
    @Transient
    protected String correspondentTypeName;

    @Getter
    @Column(name = "case_uuid")
    protected UUID caseUUID;

    @Setter
    @Getter
    @Column(name = "fullname")
    protected String fullName;

    @Setter
    @Getter
    @Column(name = "organisation")
    protected String organisation;

    @Setter
    @Getter
    @Column(name = "postcode")
    protected String postcode;

    @Setter
    @Getter
    @Column(name = "address1")
    protected String address1;

    @Setter
    @Getter
    @Column(name = "address2")
    protected String address2;

    @Setter
    @Getter
    @Column(name = "address3")
    protected String address3;

    @Setter
    @Getter
    @Column(name = "country")
    protected String country;

    @Setter
    @Getter
    @Column(name = "telephone")
    protected String telephone;

    @Setter
    @Getter
    @Column(name = "email")
    protected String email;

    @Getter
    @Column(name = "external_key")
    protected String externalKey;

    @Setter
    @Getter
    @Column(name = "reference")
    protected String reference;

    @Setter
    @Getter
    @Column(name = "deleted")
    protected boolean deleted;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Correspondent that = (Correspondent) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
