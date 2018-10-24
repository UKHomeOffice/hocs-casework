package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityCreationException;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "correspondent")
public class Correspondent {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "uuid")
    @Getter
    private UUID uuid;

    @Column(name = "created")
    @Getter
    private LocalDateTime created;

    @Column(name = "type")
    @Getter
    private String type;

    @Column(name = "case_uuid")
    @Getter
    private UUID caseUUID;

    @Column(name = "fullname")
    @Getter
    private String fullName;

    @Column(name = "postcode")
    @Getter
    private String postcode;

    @Column(name = "address1")
    @Getter
    private String address1;

    @Column(name = "address2")
    @Getter
    private String address2;

    @Column(name = "address3")
    @Getter
    private String address3;

    @Column(name = "country")
    @Getter
    private String country;

    @Column(name = "telephone")
    @Getter
    private String telephone;

    @Column(name = "email")
    @Getter
    private String email;

    @Column(name = "reference")
    @Getter
    private String reference;

    @Column(name = "deleted")
    @Getter
    private boolean deleted = Boolean.FALSE;

    public Correspondent(UUID caseUUID, CorrespondentType correspondentType, String fullName, Address address, String telephone, String email, String reference) {
        if (caseUUID == null || correspondentType == null || fullName == null) {
            throw new EntityCreationException("Cannot create Correspondent(%s, %s, %s, %s, %s, %s).", caseUUID, correspondentType, fullName, "Address", telephone, email);
        }

        this.uuid = UUID.randomUUID();
        this.created = LocalDateTime.now();
        this.caseUUID = caseUUID;
        this.type = correspondentType.toString();
        this.fullName = fullName;
        this.postcode = address.getPostcode();
        this.address1 = address.getAddress1();
        this.address2 = address.getAddress2();
        this.address3 = address.getAddress3();
        this.country = address.getCountry();
        this.telephone = telephone;
        this.email = email;
        this.reference = reference;
    }

    public CorrespondentType getCorrespondentType() {
        return CorrespondentType.valueOf(this.type);
    }

}