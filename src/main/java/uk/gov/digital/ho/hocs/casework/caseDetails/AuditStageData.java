package uk.gov.digital.ho.hocs.casework.caseDetails;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_stage_data")
@Getter
@NoArgsConstructor
public class AuditStageData implements Serializable {

    private AuditStageData(UUID stageUUID, String name, String data, UUID caseUUID, int schemaVersion, LocalDateTime created) {
        this.uuid = stageUUID;
        this.name = name;
        this.data = data;
        this.caseUUID = caseUUID;
        this.schemaVersion = schemaVersion;
        this.created = created;
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name ="uuid")
    private UUID uuid;

    @Column(name ="name")
    private String name;

    @Setter
    @Column(name ="data")
    private String data;

    @Column(name = "case_uuid")
    private UUID caseUUID;

    @Setter
    @Column(name = "schema_version")
    private int schemaVersion;

    @Column(name = "created")
    private LocalDateTime created;

    public static AuditStageData from(StageDetails stageDetails)
    {
        return new AuditStageData(stageDetails.getUuid(), stageDetails.getName(), stageDetails.getData(), stageDetails.getCaseUUID(), stageDetails.getSchemaVersion(), stageDetails.getCreated());
    }
}
