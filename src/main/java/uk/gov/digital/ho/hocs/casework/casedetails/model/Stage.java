package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Entity
@Table(name = "stage")
public class Stage implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Getter
    @Column(name = "uuid")
    private UUID uuid;

    @Getter
    @Column(name = "created")
    private LocalDateTime created;

    @Getter
    @Column(name = "type")
    private String type;

    @Column(name = "completed")
    private boolean completed = Boolean.FALSE;

    @Getter
    @Column(name = "case_uuid")
    private UUID caseUUID;

    @Getter
    @Column(name = "team_uuid")
    private UUID teamUUID;

    @Getter
    @Column(name = "user_uuid")
    private UUID userUUID;

    @Getter
    @Column(name = "case_reference", insertable = false)
    private String caseReference;

    @Column(name = "case_type", insertable = false)
    private String caseType;

    @Column(name = "data", insertable = false)
    private String data;

    @Getter
    @Column(name = "deadline", insertable = false)
    private String deadline;

    public Stage(UUID caseUUID, StageType stageType, UUID teamUUID, UUID userUUID) {
        if (caseUUID == null || stageType == null || teamUUID == null) {
            throw new EntityCreationException("Cannot create Stage(%s, %s, %s, %s).", caseUUID, stageType, teamUUID, userUUID);
        }

        this.uuid = UUID.randomUUID();
        this.created = LocalDateTime.now();
        this.type = stageType.toString();
        this.caseUUID = caseUUID;
        this.teamUUID = teamUUID;
        this.userUUID = userUUID;
    }

    public StageType getStageType() {
        return StageType.valueOf(this.type);
    }

    public CaseDataType getCaseType() {
        return CaseDataType.valueOf(this.caseType);
    }
}