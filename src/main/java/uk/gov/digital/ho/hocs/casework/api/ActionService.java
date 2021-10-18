package uk.gov.digital.ho.hocs.casework.api;

import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDto;

import java.util.UUID;

public interface ActionService {

    String getActionName();

    void create(UUID caseUuid, UUID stageUuid, String caseType, ActionDataDto actionData);

    void update(UUID caseUUID, UUID stageUUID, String caseType, ActionDataDto actionData);
}
