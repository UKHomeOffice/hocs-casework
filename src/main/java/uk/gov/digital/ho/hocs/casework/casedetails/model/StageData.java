package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stage_data")
@NoArgsConstructor
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

    @Column(name = "timestamp")
    @Getter
    private LocalDateTime timestamp = LocalDateTime.now();

    @Transient
    @Getter
    @Setter
    private CaseInputData caseInputData;

    public StageData(UUID caseUUID, StageType stageType) {
        if (caseUUID == null || stageType == null) {
            throw new EntityCreationException("Cannot create StageData(%s, %s).", caseUUID, stageType);
        }
        this.uuid = UUID.randomUUID();
        this.stageType = stageType.toString();
        this.caseUUID = caseUUID;
    }

    public StageType getType() {
        return StageType.valueOf(this.stageType);
    }

    public void allocate(UUID teamUUID, UUID userUUID) {
        if (teamUUID == null) {
            throw new EntityCreationException("Cannot call StageData.allocate(%s, %s).", teamUUID, userUUID);
        }
        this.teamUUID = teamUUID;
        this.userUUID = userUUID;
    }

}