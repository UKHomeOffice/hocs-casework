package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Entity
@Table(name = "correspondent")
public class Correspondent {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Column(name = "uuid")
    private UUID uuid;

    @Getter
    @Column(name = "created")
    private LocalDateTime created;

    @Getter
    @Column(name = "type")
    private String correspondentType;

    @Getter
    @Column(name = "case_uuid")
    private UUID caseUUID;

    @Getter
    @Column(name = "fullname")
    private String fullName;

    @Getter
    @Column(name = "postcode")
    private String postcode;

    @Getter
    @Column(name = "address1")
    private String address1;

    @Getter
    @Column(name = "address2")
    private String address2;

    @Getter
    @Column(name = "address3")
    private String address3;

    @Getter
    @Column(name = "country")
    private String country;

    @Getter
    @Column(name = "telephone")
    private String telephone;

    @Getter
    @Column(name = "email")
    private String email;

    @Getter
    @Column(name = "reference")
    private String reference;

    public Correspondent(UUID caseUUID, String correspondentType, String fullName, Address address, String telephone, String email, String reference) {
        if (caseUUID == null || correspondentType == null) {
            throw new ApplicationExceptions.EntityCreationException(String.format("Cannot create Correspondent(%s, %s, %s, %s, %s, %s).", caseUUID, correspondentType, fullName, "Address", telephone, email), LogEvent.CORRESPONDENT_CREATE_FAILURE);
        }

        this.uuid = UUID.randomUUID();
        this.created = LocalDateTime.now();
        this.caseUUID = caseUUID;
        this.correspondentType = correspondentType;
        this.fullName = fullName;
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
    }

}