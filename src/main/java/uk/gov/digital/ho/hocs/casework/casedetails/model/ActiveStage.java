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
    @Getter
    private CaseType caseType;

    @Column(name = "case_type_display")
    @Getter
    private String caseTypeDisplay;

    @Column(name = "stage_type")
    @Getter
    private StageType stageType;

    @Column(name = "stage_type_display")
    @Getter
    private String stageTypeDisplay;

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

    public ActiveStage(UUID caseUUID,
                       UUID stageUUID,
                       String caseReference,
                       CaseType caseType,
                       StageType stageType,
                       String assignedTeam,
                       String assignedTeamDisplay,
                       String assignedUser,
                       String assignedUserDisplay) {
        this.caseUUID = caseUUID;
        this.stageUUID = stageUUID;
        this.caseReference = caseReference;
        this.caseType = caseType;
        // TODO: Casetype prettyprint values.
        this.caseTypeDisplay = caseType.toString();
        this.stageType = stageType;
        this.stageTypeDisplay = stageType.getStringValue();
        // TODO: User and Team models.
        this.assignedTeam = assignedTeam;
        this.assignedTeamDisplay = assignedTeamDisplay;
        this.assignedUser = assignedUser;
        this.assignedUserDisplay = assignedUserDisplay;

    }
}
