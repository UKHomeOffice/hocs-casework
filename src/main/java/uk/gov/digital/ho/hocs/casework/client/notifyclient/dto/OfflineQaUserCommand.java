package uk.gov.digital.ho.hocs.casework.client.notifyclient.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class OfflineQaUserCommand {


    private String command = "offline_qa_user";
    private UUID caseUUID;
    private UUID stageUUID;
    private String caseReference;
    private UUID offlineQaUserUUID;
    private UUID currentUserUUID;

    public OfflineQaUserCommand(UUID caseUUID, UUID stageUUID, String caseReference, UUID offlineQaUserUUID, UUID currentUserUUID){
        this.caseUUID = caseUUID;
        this.stageUUID = stageUUID;
        this.caseReference = caseReference;
        this.offlineQaUserUUID = offlineQaUserUUID;
        this.currentUserUUID = currentUserUUID;
    }

}