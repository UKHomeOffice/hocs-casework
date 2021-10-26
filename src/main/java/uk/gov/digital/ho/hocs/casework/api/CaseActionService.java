package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDto;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseActionDataResponseDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.CaseTypeActionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class CaseActionService {

    private final CaseDataRepository caseDataRepository;
    private final InfoClient infoClient;

    private final Map<String, ActionService> actionServiceMap = new HashMap<>();

    public CaseActionService(CaseDataRepository caseDataRepository, InfoClient infoClient, List<ActionService> actionServices ) {
        this.caseDataRepository = caseDataRepository;
        this.infoClient = infoClient;

        log.info("Loading ActionService implementations.");

        for (ActionService actionService : actionServices) {
            this.actionServiceMap.putIfAbsent(actionService.getServiceDtoTypeKey(), actionService);
        }

        log.info("Loaded {} ActionService implementations: {}", actionServiceMap.size(), actionServiceMap.keySet());
    }

    public void createActionDataForCase(UUID caseUUID, UUID stageUUID, ActionDataDto actionData) {
        ActionService typeServiceInstance = getActionServiceInstance(actionData);

        if (typeServiceInstance != null) {
            typeServiceInstance.create(caseUUID, stageUUID, actionData);
        } else {
            throw new UnsupportedOperationException(String.format("No Service available to CREATE actionDataDto's of type: %s",actionData.getClass().getSimpleName()));
        }
    }

    public void updateActionDataForCase(UUID caseUUID, UUID stageUUID, UUID actionEntityId, ActionDataDto actionData) {
        ActionService typeServiceInstance = getActionServiceInstance(actionData);

        if (typeServiceInstance != null) {
            typeServiceInstance.update(caseUUID, stageUUID, actionEntityId, actionData);
        } else {
            throw new UnsupportedOperationException(String.format("No Service available to UPDATE actionDataDto's of type: %s",actionData.getClass().getSimpleName()));
        }
    }

    public CaseActionDataResponseDto getAllCaseActionDataForCase(UUID caseId) {
        log.debug("Received request for all case action data for caseId: {}", caseId);

        Map<String, List<ActionDataDto>> actions = new HashMap<>();

        getAllActionsForCaseById(caseId, actions);

        CaseData caseData = caseDataRepository.findActiveByUuid(caseId);
        List<CaseTypeActionDto> caseTypeActionDtoList =  infoClient.getCaseTypeActionForCaseType(caseData.getType());

        log.info("Returning case action data for caseId: {}", caseId);
        return CaseActionDataResponseDto.from(actions, caseTypeActionDtoList, caseData.getCaseDeadline());
    }

    public void getAllActionsForCaseById(UUID caseId, Map<String, List<ActionDataDto>> caseActionDataMap) {
        Collection<ActionService> actionServices = this.actionServiceMap.values();

        actionServices.forEach(actionService -> {
            caseActionDataMap.put(actionService.getServiceMapKey(), actionService.getAllActionsForCase(caseId));
        });
    }

    private ActionService getActionServiceInstance(ActionDataDto actionDataDto) {
        return actionServiceMap.get(actionDataDto.getClass().getSimpleName());
    }
}
