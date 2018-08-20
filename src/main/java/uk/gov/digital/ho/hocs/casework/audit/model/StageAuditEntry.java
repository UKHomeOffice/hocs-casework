package uk.gov.digital.ho.hocs.casework.audit.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_stage_data")
@EqualsAndHashCode
public class StageAuditEntry implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name ="uuid")
    @Getter
    private UUID uuid;

    @Column(name = "type")
    @Getter
    private String type;

    @Column(name = "timestamp")
    @Getter
    private LocalDateTime timestamp;

    @Column(name = "case_uuid")
    @Getter
    private UUID caseUUID;

    private StageAuditEntry(UUID stageUUID, String type, UUID caseUUID, LocalDateTime timestamp) {
        this.uuid = stageUUID;
        this.type = type;
        this.caseUUID = caseUUID;
        this.timestamp = timestamp;
    }

    public static StageAuditEntry from(StageData stageData)
    {
        if (stageData != null) {
            return new StageAuditEntry(stageData.getUuid(), stageData.getType().toString(), stageData.getCaseUUID(), stageData.getCreated());
        } else {
            return null;
        }
    }
}
