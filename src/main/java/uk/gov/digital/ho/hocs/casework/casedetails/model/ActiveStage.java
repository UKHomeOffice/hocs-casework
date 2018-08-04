package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "active_stage")
@NoArgsConstructor
public class ActiveStage implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "case_uuid")
    @Getter
    private UUID caseUUID;

    @Column(name = "stage_uuid")
    @Getter
    private UUID stageUUID;

    @Column(name = "case_reference")
    @Getter
    private String caseReference;

    @Column(name = "case_type")
    private String caseType;

    @Column(name = "stage_type")
    private String stageType;

    @Column(name = "assigned_team")
    @Getter
    private String assignedTeam;

    @Column(name = "assigned_team_display")
    @Getter
    private String assignedTeamDisplay;

    @Column(name = "assigned_user")
    @Getter
    private String assignedUser;

    @Column(name = "assigned_user_display")
    @Getter
    private String assignedUserDisplay;

    public ActiveStage(CaseData caseData,
                       StageData stageData,
                       String assignedTeam,
                       String assignedTeamDisplay,
                       String assignedUser,
                       String assignedUserDisplay) {
        this.caseUUID = caseData.getUuid();
        this.stageUUID = stageData.getUuid();
        this.caseReference = caseData.getReference();
        this.caseType = caseData.getType().toString();
        this.stageType = stageData.getType().toString();
        // TODO: User and Team models.
        this.assignedTeam = assignedTeam;
        this.assignedTeamDisplay = assignedTeamDisplay;
        this.assignedUser = assignedUser;
        this.assignedUserDisplay = assignedUserDisplay;

    }

    public StageType getStageType() {
        return StageType.valueOf(this.stageType);
    }

    public CaseType getCaseType() {
        return CaseType.valueOf(this.caseType);
    }
}
