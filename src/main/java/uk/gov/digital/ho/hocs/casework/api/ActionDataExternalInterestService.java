package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.*;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.CaseTypeActionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.*;
import uk.gov.digital.ho.hocs.casework.domain.repository.ActionDataExternalInterestRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.ACTION_DATA_UPDATE_FAILURE;

@Service
@Slf4j
public class ActionDataExternalInterestService implements ActionService {
    private final ActionDataExternalInterestRepository actionDataExternalInterestRepository;
    private final CaseDataRepository caseDataRepository;
    private final InfoClient infoClient;
    private final AuditClient auditClient;
    private final CaseNoteService caseNoteService;

    private static final String CREATE_CASE_NOTE_KEY = "RECORD_INTEREST";
    private static final String UPDATE_CASE_NOTE_KEY = "UPDATE_INTEREST";

    @Autowired
    public ActionDataExternalInterestService(ActionDataExternalInterestRepository actionDataExternalInterestRepository,
                                             CaseDataRepository caseDataRepository,
                                             InfoClient infoClient,
                                             AuditClient auditClient,
                                             CaseNoteService caseNoteService) {
        this.actionDataExternalInterestRepository = actionDataExternalInterestRepository;
        this.caseDataRepository = caseDataRepository;
        this.infoClient = infoClient;
        this.auditClient = auditClient;
        this.caseNoteService = caseNoteService;
    }

    @Override
    public String getServiceDtoTypeKey() {
        return ActionDataExternalInterestInboundDto.class.getSimpleName();
    }

    @Override
    public String getServiceMapKey() {
        return "recordInterest";
    }

    @Override
    public void create(UUID caseUuid, UUID stageUuid, ActionDataDto actionData) {

        ActionDataExternalInterestInboundDto actionDataExternalInterestDto =
                (ActionDataExternalInterestInboundDto) actionData;


        log.debug("Received request to create external " +
                "interest: {} for case: {}, stage: {}", actionDataExternalInterestDto, caseUuid, stageUuid);


        CaseData caseData = caseDataRepository.findActiveByUuid(caseUuid);
        if (caseData == null) {
            // Should have exited from the getCase call if no case with ID, however put here for safety to stop orphaned records.
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case with id: %s does not exist.", caseUuid), CASE_NOT_FOUND);
        }

        CaseTypeActionDto caseTypeActionDto
                = infoClient.getCaseTypeActionByUuid(caseData.getType(), actionData.getCaseTypeActionUuid());
        if (caseTypeActionDto == null) {
            throw new ApplicationExceptions.EntityNotFoundException(
                    String.format("No Case Type Action found for actionId: %s", actionData.getUuid())
                    , ACTION_DATA_CREATE_FAILURE);
        }


        ActionDataExternalInterest actionDataExternalInterest = new ActionDataExternalInterest(
                actionData.getCaseTypeActionUuid(),
                actionData.getCaseTypeActionLabel(),
                caseData.getType(),
                caseUuid,
                actionDataExternalInterestDto.getInterestedPartyType(),
                actionDataExternalInterestDto.getDetailsOfInterest()
        );

        caseNoteService.createCaseNote(caseUuid, CREATE_CASE_NOTE_KEY,
                actionDataExternalInterest.getDetailsOfInterest());
        actionDataExternalInterestRepository.save(actionDataExternalInterest);

        auditClient.createExternalInterestAudit(actionDataExternalInterest);

        log.info("Created action:  {} for case: {}", actionData, caseUuid);
    }

    @Override
    public void update(UUID caseUuid, UUID stageUuid, UUID actionEntityId, ActionDataDto updatedActionData) {
        ActionDataExternalInterestInboundDto updateExternalInterestDto = (ActionDataExternalInterestInboundDto) updatedActionData;
        log.debug("Received request to update external interest: {} for case: {}, stage: {}, caseDataType: {}", updateExternalInterestDto, caseUuid, stageUuid);

        UUID externalInterestUuid = updatedActionData.getUuid();

        CaseData caseData = caseDataRepository.findActiveByUuid(caseUuid);
        if (caseData == null) {
            // Should have exited from the getCase call if no case with ID, however put here for safety to stop orphaned records.
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Case with id: %s does not exist.", caseUuid), CASE_NOT_FOUND);
        }

        CaseTypeActionDto caseTypeActionDto = infoClient.getCaseTypeActionByUuid(caseData.getType(), updateExternalInterestDto.getCaseTypeActionUuid());
        if (caseTypeActionDto == null) {
            throw new ApplicationExceptions.EntityNotFoundException
                    (String.format("No Case Type Action found for actionId: %s", externalInterestUuid), ACTION_DATA_UPDATE_FAILURE);
        }

        ActionDataExternalInterest existingExternalInterestData =
                actionDataExternalInterestRepository.findByUuidAndCaseDataUuid(externalInterestUuid, caseUuid);

        if (existingExternalInterestData == null) {
            throw new ApplicationExceptions.EntityNotFoundException
                    (String.format("Action with id: %s does not exist.", externalInterestUuid), ACTION_DATA_UPDATE_FAILURE);
        }

        existingExternalInterestData.setDetailsOfInterest(updateExternalInterestDto.getDetailsOfInterest());
        existingExternalInterestData.setPartyType(updateExternalInterestDto.getInterestedPartyType());

        caseNoteService.createCaseNote(caseUuid, UPDATE_CASE_NOTE_KEY,
                existingExternalInterestData.getDetailsOfInterest());
        actionDataExternalInterestRepository.save(existingExternalInterestData);

        auditClient.updateExternalInterestAudit(existingExternalInterestData);
    }

    @Override
    public List<ActionDataDto> getAllActionsForCase(UUID caseUUID) {
        List<ActionDataExternalInterest> externalInterests =
                actionDataExternalInterestRepository.findAllByCaseDataUuid(caseUUID);
        log.info("Returning {} Extensions for caseIsd: {}", externalInterests.size(), caseUUID);

        return externalInterests.stream().map(interest ->
                {
                    Map<String, String> interestedPartyEntity =
                            infoClient.getEntityBySimpleName(interest.getPartyType()).getData();

                    return new ActionDataExternalInterestOutboundDto(
                            interest.getUuid(),
                            interest.getCaseTypeActionUuid(),
                            interest.getCaseTypeActionLabel(),
                            interest.getPartyType(),
                            interestedPartyEntity,
                            interest.getDetailsOfInterest());
                }
        ).collect(Collectors.toList());
    }
}
