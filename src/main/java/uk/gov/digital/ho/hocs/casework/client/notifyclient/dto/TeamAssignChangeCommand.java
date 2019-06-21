package uk.gov.digital.ho.hocs.casework.client.notifyclient.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class TeamAssignChangeCommand {


    private String command = "team_assign_change";
    private UUID caseUUID;
    private UUID stageUUID;
    private String caseReference;
    private UUID teamUUID;
    private String allocationType;

    public TeamAssignChangeCommand(UUID caseUUID, UUID stageUUID, String caseReference, UUID teamUUID, String allocationType){
        this.caseUUID = caseUUID;
        this.stageUUID = stageUUID;
        this.caseReference = caseReference;
        this.teamUUID = teamUUID;
        this.allocationType = allocationType;
    }

}