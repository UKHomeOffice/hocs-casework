package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;

import javax.persistence.*;
import java.util.UUID;

@NoArgsConstructor
@Entity
@Table(name = "reference_data")
public class ReferenceData {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Getter
    @Column(name = "case_uuid")
    private UUID caseUUID;

    @Getter
    @Column(name = "reference")
    private String reference;

    @Getter
    @Column(name = "type")
    private String type;


    public ReferenceData(UUID caseUUID, ReferenceType referenceType, String reference) {
        if (caseUUID == null || referenceType == null || reference == null) {
            throw new EntityCreationException("Cannot create ReferenceData(%s, %s, %s).", caseUUID, referenceType, reference);
        }

        this.caseUUID = caseUUID;
        this.type = referenceType.toString();
        this.reference = reference;
    }

    public ReferenceType getReferenceType() {
        return ReferenceType.valueOf(this.type);
    }
}

