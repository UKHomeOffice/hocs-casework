package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class CaseActionService {

    Map<String, ActionService> actionServiceMap = new HashMap<>();

    @Autowired
    private void setActionTypes(List<ActionService> actionServices) {
        log.info("Loading ActionService implementations.");
        for (ActionService actionService : actionServices) {
            actionServiceMap.putIfAbsent(actionService.getActionName(), actionService);
        }
        log.info("Loaded {} ActionService implementations: {}", actionServiceMap.size(), actionServiceMap.keySet());
    }

    public void createActionDataForCase(UUID caseUUID, UUID stageUuid, String caseType, ActionDataDto actionData) {
        ActionService typeServiceInstance = getActionServiceInstance(actionData);

        if (typeServiceInstance != null) {
            typeServiceInstance.create(caseUUID, stageUuid, caseType, actionData);
        } else {
            throw new UnsupportedOperationException(String.format("There is no Service available to process actionDataDto's of type: %s",actionData.getClass().getSimpleName()));
        }
    }

    private ActionService getActionServiceInstance(ActionDataDto actionDataDto) {
        return actionServiceMap.get(actionDataDto.getClass().getSimpleName());
    }
}
