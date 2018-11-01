package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityCreationException;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
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

    @Column(name = "type")
    private String type;

    @Getter
    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "status")
    private String status;

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

    @Getter
    @Column(name = "data", insertable = false)
    private String data;

    public Stage(UUID caseUUID, StageType stageType, UUID teamUUID, UUID userUUID, StageStatusType stageStatusType) {
        validateCreate(caseUUID, stageType);

        this.uuid = UUID.randomUUID();
        this.created = LocalDateTime.now();
        this.caseUUID = caseUUID;
        this.type = stageType.toString();
        update(teamUUID, userUUID, stageStatusType);
    }

    public StageType getStageType() {
        return StageType.valueOf(this.type);
    }

    public StageStatusType getStageStatusType() {
        return StageStatusType.valueOf(this.status);
    }

    private static void validateCreate(UUID caseUUID, StageType stageType) {
        if (caseUUID == null || stageType == null) {
            throw new EntityCreationException("Cannot create Stage (%s, %s).", caseUUID, stageType);
        }
    }

    private static void validateUpdate(UUID teamUUID, StageStatusType stageStatusType) {
        if (teamUUID == null || stageStatusType == null) {
            throw new EntityCreationException("Cannot update Stage (%s, %s).", teamUUID, stageStatusType);
        }
    }

    public CaseDataType getCaseDataType() {
        if (this.caseType == null) {
            return null;
        } else {
            return CaseDataType.valueOf(this.caseType);
        }
    }

    public void update(UUID teamUUID, UUID userUUID, StageStatusType stageStatusType) {
        validateUpdate(teamUUID, stageStatusType);

        this.teamUUID = teamUUID;
        this.userUUID = userUUID;
        this.status = stageStatusType.toString();
    }
}