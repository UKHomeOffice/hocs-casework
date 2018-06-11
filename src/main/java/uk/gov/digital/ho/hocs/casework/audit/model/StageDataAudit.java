package uk.gov.digital.ho.hocs.casework.audit.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageData;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_stage_data")
@Getter
@NoArgsConstructor
public class StageDataAudit implements Serializable {

    @Column(name = "type")
    private String type;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name ="uuid")
    private UUID uuid;
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Setter
    @Column(name ="data")
    private String data;

    @Column(name = "case_uuid")
    private UUID caseUUID;

    private StageDataAudit(UUID stageUUID, String type, String data, UUID caseUUID, LocalDateTime timestamp) {
        this.uuid = stageUUID;
        this.type = type;
        this.data = data;
        this.caseUUID = caseUUID;
        this.timestamp = timestamp;
    }

    public static StageDataAudit from(StageData stageData)
    {
        return new StageDataAudit(stageData.getUuid(), stageData.getType(), stageData.getData(), stageData.getCaseUUID(), stageData.getTimestamp());
    }
}
