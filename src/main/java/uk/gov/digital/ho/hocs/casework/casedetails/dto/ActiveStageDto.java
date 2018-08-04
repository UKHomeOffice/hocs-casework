package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ActiveStage;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ActiveStageDto implements Serializable {

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("stageUUID")
    private UUID stageUUID;

    @JsonProperty("caseReference")
    private String caseReference;

    @JsonProperty("caseType")
    private CaseType caseType;

    @JsonProperty("caseTypeDisplay")
    private String caseTypeDisplay;

    @JsonProperty("stageType")
    private StageType stageType;

    @JsonProperty("stageTypeDisplay")
    private String stageTypeDisplay;

    @JsonProperty("assignedTeam")
    private String assignedTeam;

    @JsonProperty("assignedTeamDisplay")
    private String assignedTeamDisplay;

    @JsonProperty("assignedUser")
    private String assignedUser;

    @JsonProperty("assignedUserDisplay")
    private String assignedUserDisplay;

    public static ActiveStageDto from(ActiveStage activeStage) {
        return new ActiveStageDto(
                activeStage.getCaseUUID(),
                activeStage.getStageUUID(),
                activeStage.getCaseReference(),
                activeStage.getCaseType(),
                activeStage.getCaseType().getDisplayValue(),
                activeStage.getStageType(),
                activeStage.getStageType().getDisplayValue(),
                activeStage.getAssignedTeam(),
                activeStage.getAssignedTeamDisplay(),
                activeStage.getAssignedUser(),
                activeStage.getAssignedUserDisplay());
    }
}
