package uk.gov.digital.ho.hocs.casework.audit.model;

import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_case_data")
public class CaseDataAudit implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "type")
    @Getter
    private String type;

    @Column(name = "reference")
    @Getter
    private String reference;

    @Column(name = "uuid")
    @Getter
    private UUID uuid;

    @Column(name = "timestamp")
    @Getter
    private LocalDateTime timestamp;

    private CaseDataAudit(String type, String reference, UUID uuid, LocalDateTime timestamp) {
        this.type = type;
        this.reference = reference;
        this.uuid = uuid;
        this.timestamp = timestamp;
    }

    public static CaseDataAudit from(CaseData caseData) {
        return new CaseDataAudit(caseData.getType(), caseData.getReference(), caseData.getUuid(), caseData.getTimestamp());
    }

}
