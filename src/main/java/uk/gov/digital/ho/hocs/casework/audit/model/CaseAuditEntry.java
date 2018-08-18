package uk.gov.digital.ho.hocs.casework.audit.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_case_data")
@EqualsAndHashCode
public class CaseAuditEntry implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "type")
    @Getter
    private String type;

    @Column(name = "uuid")
    @Getter
    private UUID uuid;

    @Column(name = "timestamp")
    @Getter
    private LocalDateTime timestamp;

    private CaseAuditEntry(String type, UUID uuid, LocalDateTime timestamp) {
        this.type = type;
        this.uuid = uuid;
        this.timestamp = timestamp;
    }

    public static CaseAuditEntry from(CaseData caseData) {
        if (caseData != null) {
            return new CaseAuditEntry(caseData.getTypeString(), caseData.getUuid(), caseData.getTimestamp());
        } else {
            return null;
        }
    }

}
