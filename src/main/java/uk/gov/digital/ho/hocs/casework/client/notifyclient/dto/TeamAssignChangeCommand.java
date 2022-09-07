package uk.gov.digital.ho.hocs.casework.client.notifyclient.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@java.lang.SuppressWarnings("squid:S1068")
@Getter
@NoArgsConstructor
public class TeamAssignChangeCommand extends NotifyCommand {

    private String command = "team_assign_change";

    private UUID teamUUID;

    private String allocationType;

    public TeamAssignChangeCommand(UUID caseUUID,
                                   UUID stageUUID,
                                   String caseReference,
                                   UUID teamUUID,
                                   String allocationType) {
        super(caseUUID, stageUUID, caseReference);
        this.teamUUID = teamUUID;
        this.allocationType = allocationType;
    }

}
