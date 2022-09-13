package uk.gov.digital.ho.hocs.casework.client.notifyclient.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@java.lang.SuppressWarnings("squid:S1068")
@Getter
@NoArgsConstructor
public abstract class NotifyCommand {

    private UUID caseUUID;

    private UUID stageUUID;

    private String caseReference;

    public NotifyCommand(UUID caseUUID, UUID stageUUID, String caseReference) {
        this.caseUUID = caseUUID;
        this.stageUUID = stageUUID;
        this.caseReference = caseReference;
    }

    public abstract String getCommand();

}