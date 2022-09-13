package uk.gov.digital.ho.hocs.casework.client.notifyclient.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@java.lang.SuppressWarnings("squid:S1068")
@Getter
@NoArgsConstructor
public class UserAssignChangeCommand extends NotifyCommand {

    private String command = "user_assign_change";

    private UUID currentUserUUID;

    private UUID newUserUUID;

    public UserAssignChangeCommand(UUID caseUUID,
                                   UUID stageUUID,
                                   String caseReference,
                                   UUID currentUserUUID,
                                   UUID newUserUUID) {
        super(caseUUID, stageUUID, caseReference);
        this.currentUserUUID = currentUserUUID;
        this.newUserUUID = newUserUUID;
    }

}
