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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class CaseActionService {

    private final CaseDataRepository caseDataRepository;

    private final InfoClient infoClient;

    private final List<ActionService> actionServiceList;

    private final DeadlineService deadlineService;

    public CaseActionService(final CaseDataRepository caseDataRepository,
                             final InfoClient infoClient,
                             final List<ActionService> actionServiceMap,
                             final DeadlineService deadlineService) {
        this.caseDataRepository = caseDataRepository;
        this.infoClient = infoClient;
        this.actionServiceList = new LinkedList<>(actionServiceMap);
        this.deadlineService = deadlineService;
    }

    public CaseActionDataResponseDto getAllCaseActionDataForCase(UUID caseId) {
        log.debug("Received request for all case action data for caseId: {}", caseId);

        Map<String, List<ActionDataDto>> actions = new HashMap<>();

        getAllActionsForCaseById(caseId, actions);

        CaseData caseData = caseDataRepository.findActiveByUuid(caseId);
        List<CaseTypeActionDto> caseTypeActionDtoList = infoClient.getCaseTypeActionForCaseType(caseData.getType());
        int remainingDays = deadlineService.calculateRemainingWorkingDaysForCaseType(caseData.getType(),
            caseData.getCaseDeadline(), LocalDate.now());

        log.info("Returning case action data for caseId: {}", caseId);
        return CaseActionDataResponseDto.from(actions, caseTypeActionDtoList, caseData.getCaseDeadline(),
            remainingDays);
    }

    public void getAllActionsForCaseById(UUID caseId, Map<String, List<ActionDataDto>> caseActionDataMap) {
        actionServiceList.forEach(
            (ActionService actionService) -> caseActionDataMap.put(actionService.getServiceMapKey(),
                actionService.getAllActionsForCase(caseId)));
    }

}
