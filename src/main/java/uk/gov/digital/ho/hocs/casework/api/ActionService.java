package uk.gov.digital.ho.hocs.casework.api;

import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDto;

import java.util.List;
import java.util.UUID;

public interface ActionService {

    String getServiceDtoTypeKey();

    String getServiceMapKey();

    void create(UUID caseUuid, UUID stageUuid, ActionDataDto actionData);

    void update(UUID caseUUID, UUID stageUUID, UUID actionEntityId, ActionDataDto actionData);

    List<ActionDataDto> getAllActionsForCase(UUID caseUUID);
}
