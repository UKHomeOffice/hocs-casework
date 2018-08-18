package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;
import lombok.Setter;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stage_data")
public class StageData implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "type")
    private String stageType;

    @Column(name = "uuid")
    @Getter
    private UUID uuid;

    @Column(name = "case_uuid")
    @Getter
    private UUID caseUUID;

    @Column(name = "team_uuid")
    @Getter
    private UUID teamUUID;

    @Column(name = "user_uuid")
    @Getter
    private UUID userUUID;

    @Column(name = "active")
    @Getter
    private boolean active;

    @Column(name = "timestamp")
    @Getter
    private LocalDateTime timestamp = LocalDateTime.now();

    @Transient
    @Getter
    @Setter
    private CaseInputData caseInputData;

    public StageData(UUID caseUUID, StageType stageType, UUID teamUUID, UUID userUUID) {
        if (caseUUID == null || stageType == null || teamUUID == null) {
            throw new EntityCreationException("Cannot create StageData(%s, %s, %s, %s).", caseUUID, stageType, teamUUID, userUUID);
        }
        this.uuid = UUID.randomUUID();
        this.stageType = stageType.toString();
        this.caseUUID = caseUUID;
        this.teamUUID = teamUUID;
        this.userUUID = userUUID;
        this.active = true;

    }

    public StageType getType() {
        return StageType.valueOf(this.stageType);
    }

}