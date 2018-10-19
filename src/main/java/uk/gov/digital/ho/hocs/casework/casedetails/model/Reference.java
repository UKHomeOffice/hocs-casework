package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reference")
public class Reference implements Serializable {

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

    @Getter
    @Column(name = "case_uuid")
    private UUID caseUUID;

    @Getter
    @Column(name = "reference")
    private String reference;

    @Getter
    @Column(name = "type")
    private String type;

    @Getter
    @Column(name = "deleted")
    private boolean deleted = Boolean.FALSE;

    public Reference(UUID caseUUID, ReferenceType referenceType, String reference) {
        if (caseUUID == null || referenceType == null || reference == null) {
            throw new EntityCreationException("Cannot create Reference(%s, %s, %s).", caseUUID, referenceType, reference);
        }

        this.uuid = UUID.randomUUID();
        this.created = LocalDateTime.now();
        this.caseUUID = caseUUID;
        this.type = referenceType.toString();
        this.reference = reference;
    }

    public ReferenceType getReferenceType() {
        return ReferenceType.valueOf(this.type);
    }

    public void delete() {
        this.deleted = Boolean.TRUE;
    }
}