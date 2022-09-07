package uk.gov.digital.ho.hocs.casework.api;

import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDto;

import java.util.List;
import java.util.UUID;

public interface ActionService {

    String getServiceMapKey();

    List<ActionDataDto> getAllActionsForCase(UUID caseUUID);

}
