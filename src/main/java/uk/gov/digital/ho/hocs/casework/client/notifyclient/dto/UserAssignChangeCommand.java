package uk.gov.digital.ho.hocs.casework.client.notifyclient.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class UserAssignChangeCommand {

    private String command = "user_assign_change";
    private UUID caseUUID;
    private UUID stageUUID;
    private String caseReference;
    private UUID currentUserUUID;
    private UUID newUserUUID;

    public UserAssignChangeCommand(UUID caseUUID, UUID stageUUID, String caseReference, UUID currentUserUUID, UUID newUserUUID){
        this.caseUUID = caseUUID;
        this.stageUUID = stageUUID;
        this.caseReference = caseReference;
        this.currentUserUUID = currentUserUUID;
        this.newUserUUID = newUserUUID;
    }
}
