package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Entity
@Table(name = "stage_data")
public class StageData implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Getter
    @Column(name = "uuid")
    private UUID uuid;

    @Getter
    @Column(name = "type")
    private String stageType;

    @Getter
    @Column(name = "case_uuid")
    private UUID caseUUID;

    @Getter
    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "updated")
    private LocalDateTime updated;

    @Getter
    @Column(name = "active")
    private boolean active;

    @Getter
    @Column(name = "team_uuid")
    private UUID teamUUID;

    @Getter
    @Column(name = "user_uuid")
    private UUID userUUID;

    @Getter
    @Setter
    @Transient
    private InputData inputData;

    public StageData(UUID caseUUID, StageType stageType, UUID teamUUID, UUID userUUID) {
        if (caseUUID == null || stageType == null || teamUUID == null) {
            throw new EntityCreationException("Cannot create StageData(%s, %s, %s, %s).", caseUUID, stageType, teamUUID, userUUID);
        }
        this.uuid = UUID.randomUUID();
        this.stageType = stageType.toString();
        this.caseUUID = caseUUID;
        this.created = LocalDateTime.now();
        this.active = true;
        this.teamUUID = teamUUID;
        this.userUUID = userUUID;

    }

    public StageType getType() {
        return StageType.valueOf(this.stageType);
    }

}