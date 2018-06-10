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

    private StageDataAudit(UUID stageUUID, String name, String data, UUID caseUUID, int schemaVersion, LocalDateTime created) {
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

    public static StageDataAudit from(StageData stageData)
    {
        return new StageDataAudit(stageData.getUuid(), stageData.getName(), stageData.getData(), stageData.getCaseUUID(), stageData.getSchemaVersion(), stageData.getCreated());
    }
}
