package uk.gov.digital.ho.hocs.casework.client.notifyclient.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@java.lang.SuppressWarnings("squid:S1068")
@Getter
@NoArgsConstructor
public class OfflineQaUserCommand extends NotifyCommand {

    private String command = "offline_qa_user";
    private UUID offlineQaUserUUID;
    private UUID currentUserUUID;

    public OfflineQaUserCommand(UUID caseUUID, UUID stageUUID, String caseReference, UUID offlineQaUserUUID, UUID currentUserUUID){
        super(caseUUID, stageUUID, caseReference);
        this.offlineQaUserUUID = offlineQaUserUUID;
        this.currentUserUUID = currentUserUUID;
    }

}