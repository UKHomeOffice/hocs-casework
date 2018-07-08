package uk.gov.digital.ho.hocs.casework.audit.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_case_data")
@NoArgsConstructor
public class CaseAuditEntry implements Serializable {

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

    private CaseAuditEntry(String type, String reference, UUID uuid, LocalDateTime timestamp) {
        this.type = type;
        this.reference = reference;
        this.uuid = uuid;
        this.timestamp = timestamp;
    }

    public static CaseAuditEntry from(CaseData caseData) {
        return new CaseAuditEntry(caseData.getType(), caseData.getReference(), caseData.getUuid(), caseData.getTimestamp());
    }

}
