package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
public class BaseCorrespondent implements Serializable {

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
    @Column(name = "case_uuid")
    protected UUID caseUUID;

    @Setter
    @Getter
    @Column(name = "fullname")
    protected String fullName;

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

}
