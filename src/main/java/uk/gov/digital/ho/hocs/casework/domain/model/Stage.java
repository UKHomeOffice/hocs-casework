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
    private Long id;

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
    @Column(name = "case_reference", insertable = false, updatable = false)
    private String caseReference;

    @Column(name = "case_type", insertable = false, updatable = false)
    private String caseType;

    @Getter
    @Column(name = "data", insertable = false, updatable = false)
    private String data;

    public Stage(UUID caseUUID, StageType stageType, UUID teamUUID, LocalDate deadline) {
        if (caseUUID == null || stageType == null) {
            throw new EntityCreationException("Cannot create Stage (%s, %s).", caseUUID, stageType);
        }

        this.uuid = UUID.randomUUID();
        this.created = LocalDateTime.now();
        this.caseUUID = caseUUID;
        this.type = stageType.toString();
        setDeadline(deadline);
        setTeam(teamUUID);
    }

    public StageType getStageType() {
        return StageType.valueOf(this.type);
    }

    public StageStatusType getStageStatusType() {
        return StageStatusType.valueOf(this.status);
    }

    public CaseDataType getCaseDataType() {
        if (this.caseType == null) {
            return null;
        } else {
            return CaseDataType.valueOf(this.caseType);
        }
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public void setTeam(UUID teamUUID) {
        this.teamUUID = teamUUID;
        this.userUUID = null;
        this.status = StageStatusType.TEAM_ASSIGNED.toString();
    }

    public void setUser(UUID userUUID) {
        this.userUUID = userUUID;
        this.status = StageStatusType.USER_ASSIGNED.toString();

    }

}