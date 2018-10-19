package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;

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

    @Column(name = "type")
    @Getter
    private String type;

    @Column(name = "deleted")
    @Getter
    private boolean deleted;

    public Correspondent(String fullName, String postcode, String address1, String address2, String address3, String country, String telephone, String email) {
        if (fullName == null) {
            throw new EntityCreationException("Cannot create Correspondent(%s, %s, %s, %s, %s, %s, %s, %s).", null, postcode, address1, address2, address3, country, telephone, email);
        }

        this.uuid = UUID.randomUUID();
        this.fullName = fullName;
        this.postcode = postcode;
        this.address1 = address1;
        this.address2 = address2;
        this.address3 = address3;
        this.country = country;
        this.telephone = telephone;
        this.email = email;
        this.created = LocalDateTime.now();
    }

    public CorrespondentType getCorrespondentType() {
        return CorrespondentType.valueOf(this.type);
    }

    public void delete() {
        this.deleted = Boolean.TRUE;
    }

}
